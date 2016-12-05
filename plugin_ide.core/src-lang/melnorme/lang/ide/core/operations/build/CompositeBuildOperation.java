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
package melnorme.lang.ide.core.operations.build;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.common.ops.Operation;
import melnorme.lang.tooling.common.ops.OperationFuture;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;

public class CompositeBuildOperation {
	
	protected final Indexable<Operation> operations;
	protected final OperationFuture<Void> opFuture;
	protected final ISchedulingRule rule; // Can be null
	
	public CompositeBuildOperation(
		IOperationMonitor monitor, 
		Indexable<Operation> operations, 
		ISchedulingRule rule
	) {
		this.operations = assertNotNull(operations);
		this.opFuture = OperationFuture.fromOperation(monitor, this::opExecute); 
		this.rule = rule;
	}
	
	public void execute() throws CommonException, OperationCancellation {
		opFuture.run();
		opFuture.getResult_forTerminated();
	}
	
	public void opExecute(IOperationMonitor monitor) throws CommonException, OperationCancellation {
		if(rule == null) {
			executeSubOperations(monitor);
		} else {
			ResourceUtils.runOperation(rule, monitor, (om) -> executeSubOperations(om));
		}
	}
	
	public void executeSubOperations(IOperationMonitor monitor) throws CommonException, OperationCancellation {
		if(monitor.isCancelled()) {
			return;
		}
		for (Operation subOperation : operations) {
			subOperation.execute(monitor);
		}
	}
	
}