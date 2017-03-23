/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.utils.concurrency;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.utilbox.concurrency.AsyncSupplier;
import melnorme.utilbox.concurrency.CancellableTask;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.fntypes.CallableX;
import melnorme.utilbox.fields.ListenerListHelper;

/**
 * Container for a data object that is created/updated concurrently by an update task.
 * 
 * This container tracks when a new data update is requested, 
 * marking the current data object as stale until the update task finishes.
 * 
 * The responsibility for actually executing the update task lies externally, not here. 
 * 
 * @param SELF must be a subtype of the parameterized class.
 */
public class ConcurrentlyDerivedData<DATA, SELF> {
	
	protected final ListenerListHelper<IDataChangedListener<SELF>> connectedListeners = 
			new ListenerListHelper<>();
	protected final ListenerListHelper<IDataUpdateRequestedListener<SELF>> updateRequestedListeners = 
			new ListenerListHelper<>();
	
	private DATA data = null;
	private DataUpdateTask<DATA> latestUpdateTask = null;
	private CountDownLatch latch = new CountDownLatch(0);
	
	public ConcurrentlyDerivedData() {
		internalSetData(null);
	}
	
	protected void internalSetData(DATA newData) {
		this.data = newData;
	}
	
	public synchronized DATA getStoredData() {
		return data;
	}
	
	@SuppressWarnings("unchecked")
	protected SELF getSelf() {
		return (SELF) this;
	}
	
	public synchronized boolean isStale() {
		return latestUpdateTask != null;
	}
	
	public synchronized boolean isStale(DATA data) {
		return isStale() || getStoredData() != data;
	}
	
	public synchronized void runSynchronized(Runnable runnable) {
		runnable.run();
	}
	
	public synchronized <R, E extends Exception> R callSynchronized(CallableX<R, E> callable) throws E {
		return callable.call();
	}
	
	public synchronized CountDownLatch getLatchForUpdateTask() {
		return latch;
	}
	
	public synchronized void setUpdateTask(DataUpdateTask<DATA> newUpdateTask) {
		assertNotNull(newUpdateTask);
		assertTrue(!newUpdateTask.hasExecuted());
		assertTrue(latestUpdateTask != newUpdateTask);
		
		if(latestUpdateTask == null) {
			assertTrue(latch.getCount() == 0);
			latch = new CountDownLatch(1);
		} else {
			assertTrue(latch.getCount() == 1);
			latestUpdateTask.cancel();
		}
		latestUpdateTask = newUpdateTask;
		
		doHandleDataUpdateRequested();
	}
	
	protected void doHandleDataUpdateRequested() {
		for(IDataUpdateRequestedListener<SELF> listener : updateRequestedListeners.getListeners()) {
			listener.dataUpdateRequested(getSelf());
		}
	}
	
	public synchronized void setNewData(DATA newData, DataUpdateTask<DATA> updateTask) {
		if(latestUpdateTask != updateTask) {
			// Ignore, means this update task was cancelled
			assertTrue(updateTask.isCancelled());
		} else {
			try {
				internalSetData(newData);
				doHandleDataChanged();
			} finally {
				latestUpdateTask = null;
				latch.countDown();
			}
		}
	}
	
	public synchronized void cancelUpdateTask(DataUpdateTask<DATA> updateTask) {
		if(latestUpdateTask == updateTask) {
			latestUpdateTask = null;
			
			latch.countDown();
		}
	}
	
	protected void doHandleDataChanged() {
		notifyStructureChanged(getSelf(), connectedListeners);
	}
	
	/* -----------------  ----------------- */
	
	public static abstract class DataUpdateTask<DATA> extends CancellableTask {
		
		protected final ConcurrentlyDerivedData<DATA, ?> derivedData;
		protected final String taskDisplayName;
		
		public DataUpdateTask(ConcurrentlyDerivedData<DATA, ?> derivedData, String taskDisplayName) {
			this.taskDisplayName = taskDisplayName;
			this.derivedData = derivedData;
		}
		
		@Override
		public String getTaskDisplayName() {
			return taskDisplayName;
		}
		
		@Override
		protected final void doRun() {
			try {
				DATA newData = createNewData();
				derivedData.setNewData(newData, this);
			} catch(OperationCancellation e) {
				derivedData.cancelUpdateTask(this);
			} catch(RuntimeException e) {
				derivedData.setNewData(null, this);
				handleRuntimeException(e);
			}
		}
		
		public void awaitUpdatedData() throws InterruptedException {
			derivedData.awaitUpdatedData();
		}
		
		protected abstract void handleRuntimeException(RuntimeException e);
		
		protected abstract DATA createNewData() throws OperationCancellation;
	}
	
	/* -----------------  ----------------- */
	
	/** 
	 * @return a {@link AsyncSupplier} that can be used to wait for the first non-stale data that becomes available.
	 */
	public DataUpdateFuture getSupplierForNextUpdate() {
		return new DataUpdateFuture();
	}
	
	public DATA awaitUpdatedData() throws InterruptedException {
		return getSupplierForNextUpdate().awaitResult();
	}
	
	public DATA awaitUpdatedData(IOperationMonitor om) throws OperationCancellation {
		return getSupplierForNextUpdate().awaitResult(om);
	}
	
	public class DataUpdateFuture implements AsyncSupplier<DATA> {
		
		public void awaitTermination() throws InterruptedException {
			getLatchForUpdateTask().await();
		}
		
		public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
			boolean success = getLatchForUpdateTask().await(timeout, unit);
			if(!success) {
				throw new TimeoutException();
			}
		}
		
		@Override
		public DATA awaitResult() throws InterruptedException {
			awaitTermination();
			return data;
		}
		
		@Override
		public DATA awaitResult(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
			awaitTermination(timeout, unit);
			return data;
		}
		
	}

	/* -----------------  ----------------- */
	
	public static interface IDataUpdateRequestedListener<DERIVED_DATA> {
		
		/** 
		 * Indicates that an asynchronous request to change the underlying derived data has been made.
		 * 
		 * This method runs under a {@link ConcurrentlyDerivedData} lock, so listeners should finish quickly.
		 */
		void dataUpdateRequested(DERIVED_DATA lockedDerivedData);
		
	}
	
	public static interface IDataChangedListener<DERIVED_DATA> {
		
		/** 
		 * Indicates that underlying derived data has changed.
		 * 
		 * This method runs under a {@link ConcurrentlyDerivedData} lock, so listeners should finish quickly.
		 */
		void dataChanged(DERIVED_DATA lockedDerivedData);
		
	}
	
	protected static <DATA> void notifyStructureChanged(final DATA lockedDerivedData, 
			ListenerListHelper<? extends IDataChangedListener<DATA>> listeners) {
		for(IDataChangedListener<DATA> listener : listeners.getListeners()) {
			listener.dataChanged(lockedDerivedData);
		}
	}
	
}