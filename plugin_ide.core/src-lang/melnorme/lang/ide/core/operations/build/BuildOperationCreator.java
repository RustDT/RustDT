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

import static java.text.MessageFormat.format;
import static melnorme.lang.ide.core.utils.TextMessageUtils.headerBIG;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.util.concurrent.Callable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import melnorme.lang.ide.core.LangCore_Actual;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.IToolOperationMonitor;
import melnorme.lang.ide.core.utils.EclipseUtils;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.ide.core.utils.TextMessageUtils;
import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.common.ops.Operation;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Collection2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;

/** 
 * A one-time {@link BuildOperationCreator} creator (is meant to be used once, immediately)
 */
public class BuildOperationCreator implements BuildManagerMessages {
	
	public static final String buildProblemId = LangCore_Actual.BUILD_PROBLEM_ID;
	
	protected final BuildManager buildMgr;
	protected final Location location;
	protected final String projectName;
	protected final IToolOperationMonitor toolMonitor;

	public BuildOperationCreator(
		BuildManager buildMgr, IProject project, IToolOperationMonitor toolMonitor
	) throws CommonException {
		this(buildMgr, ResourceUtils.getLocation(project), project.getName(), toolMonitor);
	}
	
	public BuildOperationCreator(
		BuildManager buildMgr, Location location, String projectName, IToolOperationMonitor toolMonitor
	) {
		this.buildMgr = assertNotNull(buildMgr);
		this.location = assertNotNull(location);
		this.projectName = assertNotNull(projectName);
		this.toolMonitor = assertNotNull(toolMonitor);
	}
	
	protected ArrayList2<Operation> operations;
	
	protected boolean addOperation(Operation toolOp) {
		return operations.add(toolOp);
	}
	
	public CompositeBuildOperation newProjectBuildOperation(
		IOperationMonitor om,
		Collection2<Operation> buildOps, 
		boolean clearMarkers
	) throws CommonException {
		operations = ArrayList2.create();
		
		if(buildOps.isEmpty()) {
			return new CompositeBuildOperation(om, operations, null);
		}
		
		addCompositeBuildOperationMessage();
		
		if(clearMarkers) {
			addOperation(new ClearMarkersOperation(location, projectName, toolMonitor));
		}
		
		if(buildOps.isEmpty()) {
			addOperation(newMessageOperation( 
				TextMessageUtils.headerSMALL(MSG_NoBuildTargetsEnabled)));
		}
		
		for(Operation buildOp : buildOps) {
			addOperation(buildOp);
		}
		
		// refresh project
		addOperation(new Operation() {
			@Override
			public void execute(IOperationMonitor om) throws CommonException, OperationCancellation {
				for (IResource resource : ResourceUtils.getResourcesAt(location)) {
					try {
						resource.refreshLocal(IResource.DEPTH_INFINITE, EclipseUtils.pm(om));
					} catch(CoreException e) {
						throw EclipseUtils.createCommonException(e);
					}
				}
			}
		});
		
		addOperation(newMessageOperation(headerBIG(MSG_BuildTerminated)));
		
		return createProjectBuildOperation(om, location);
	}
	
	public ProjectBuildOperation createProjectBuildOperation(IOperationMonitor om, Location location) {
		ISchedulingRule rule = getRuleForOperation();
		return new ProjectBuildOperation(om, location, operations, rule);
	}
	
	public ISchedulingRule getRuleForOperation() {
		// Note: the locking rule has to be the whole workspace, because the build might read dependent projects
		// and also error markers can be created globally
		return ResourceUtils.getWorkspaceRoot();
	}
	
	public class ProjectBuildOperation extends CompositeBuildOperation {
		
		protected final Location location;
		
		public ProjectBuildOperation(
			IOperationMonitor monitor, Location location, Indexable<Operation> operations, ISchedulingRule rule
		) {
			super(monitor, operations, rule);
			this.location = assertNotNull(location);
		}
		
		public Location getLocation() {
			return location;
		}
		
		@Override
		public void opExecute(IOperationMonitor monitor) throws CommonException, OperationCancellation {
			// Note, must do this before any lock, so that previous op can be cancelled:
			buildMgr.setNewBuildOperation(this);
			
			super.opExecute(monitor);
		}
		
		public boolean tryCancel() {
			return opFuture.tryCancel();
		}
		
	}
	
	protected void addCompositeBuildOperationMessage() throws CommonException {
		String startMsg = headerBIG(format(MSG_BuildingProject, LangCore_Actual.NAME_OF_LANGUAGE, projectName));
		addOperation(newMessageOperation(startMsg));
	}
	
	protected Operation newMessageOperation(String msg) {
		return new BuildMessageOperation(msg);
	}
	
	protected class BuildMessageOperation implements Operation, Callable<Void> {
		
		protected final String msg;
		
		public BuildMessageOperation(String msg) {
			this.msg = msg;
		}
		
		@Override
		public void execute(IOperationMonitor monitor) {
			executeDo();
		}
		
		protected void executeDo() {
			call();
		}
		
		@Override
		public Void call() throws RuntimeException {
			toolMonitor.writeInfoMessage(msg);
			return null;
		}
	}
	
}