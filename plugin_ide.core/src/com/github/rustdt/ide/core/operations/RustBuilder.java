/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.core.operations;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.github.rustdt.tooling.RustBuildOutputParser;
import com.github.rustdt.tooling.ops.RustSDKLocationValidator;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.BuildTarget;
import melnorme.lang.ide.core.operations.BuildTargetsProjectBuilder;
import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.data.PathValidator;
import melnorme.lang.tooling.ops.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Rust builder, using Cargo.
 */
public class RustBuilder extends BuildTargetsProjectBuilder {
	
	public RustBuilder() {
	}
	
	@Override
	protected PathValidator getBuildToolPathValidator() {
		return new RustSDKLocationValidator();
	}
	
	@Override
	protected ProcessBuilder createCleanPB() throws CoreException, CommonException {
		return createSDKProcessBuilder("clean");
	}
	
	/* ----------------- Build ----------------- */
	
	@Override
	protected CommonBuildTargetOperation newBuildTargetOperation(OperationInfo parentOpInfo, IProject project,
			BuildTarget buildTarget) {
		return new RustRunBuildOperationExtension(parentOpInfo, buildTarget);
	}
	
	protected class RustRunBuildOperationExtension extends CommonBuildTargetOperation {
		
		public RustRunBuildOperationExtension(OperationInfo parentOpInfo, BuildTarget buildTarget) {
			super(parentOpInfo, buildTarget);
		}
		
		@Override
		public IProject[] execute(IProject project, int kind, Map<String, String> args, IProgressMonitor monitor)
				throws CoreException, CommonException, OperationCancellation {
			
			ArrayList2<String> buildCommands = new ArrayList2<>();
			
			if(getBuildTargetName() == null) {
				buildCommands.add("build");
			} else {
				// TODO: properly implement other test targets
				buildCommands.addElements("test", "--no-run");
			}
			
			ProcessBuilder pb = createSDKProcessBuilder(buildCommands.toArray(String.class));
			
			ExternalProcessResult buildAllResult = runBuildTool_2(monitor, pb);
			doBuild_processBuildResult(buildAllResult);
			
			return null;
		}
		
		protected void doBuild_processBuildResult(ExternalProcessResult buildAllResult) 
				throws CoreException, CommonException {
			ArrayList<ToolSourceMessage> buildMessage = new RustBuildOutputParser() {
				@Override
				protected void handleMessageParseError(CommonException ce) {
					 LangCore.logStatus(LangCore.createCoreException(ce));
				}
			}.parseOutput(buildAllResult);
			
			addErrorMarkers(buildMessage, ResourceUtils.getProjectLocation(getProject()));
		}
	}
	
}