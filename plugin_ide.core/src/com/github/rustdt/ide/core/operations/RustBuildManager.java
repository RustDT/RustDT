/*******************************************************************************
 * Copyright (c) 2015, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.core.operations;

import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.github.rustdt.tooling.RustBuildOutputParser;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.lang.ide.core.operations.ToolMarkersUtil;
import melnorme.lang.ide.core.operations.build.BuildManager;
import melnorme.lang.ide.core.operations.build.BuildTarget;
import melnorme.lang.ide.core.operations.build.CommonBuildTargetOperation;
import melnorme.lang.ide.core.project_model.BundleManifestResourceListener;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.ops.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Rust builder, using Cargo.
 */
public class RustBuildManager extends BuildManager {
	
	@Override
	protected BundleManifestResourceListener init_createResourceListener() {
		return new ManagerResourceListener(new org.eclipse.core.runtime.Path("Cargo.toml"));
	}
	
	@Override
	public CommonBuildTargetOperation createBuildTargetSubOperation(OperationInfo parentOpInfo, IProject project,
			Path buildToolPath, BuildTarget buildTarget, boolean fullBuild) {
		return new RustBuildTargetOperation(parentOpInfo, project, buildToolPath, buildTarget, fullBuild);
	}
	
	/* ----------------- Build ----------------- */
	
	protected class RustBuildTargetOperation extends CommonBuildTargetOperation {
		
		public RustBuildTargetOperation(OperationInfo parentOpInfo, IProject project,
				Path buildToolPath, BuildTarget buildTarget, boolean fullBuild) {
			super(parentOpInfo, project, buildToolPath, buildTarget, fullBuild);
		}
		
		@Override
		public void execute(IProgressMonitor pm) throws CoreException, CommonException, OperationCancellation {
			
			ArrayList2<String> buildCommands = new ArrayList2<>();
			
			if(getBuildTargetName() == null) {
				buildCommands.add("build");
			} else {
				// TODO: properly implement other test targets
				buildCommands.addElements("test", "--no-run");
			}
			
			ProcessBuilder pb = getToolManager().createSDKProcessBuilder(getProject(), 
				buildCommands.toArray(String.class));
			
			ExternalProcessResult buildAllResult = runBuildTool(pm, pb);
			doBuild_processBuildResult(buildAllResult);
		}
		
		protected void doBuild_processBuildResult(ExternalProcessResult buildAllResult) 
				throws CoreException, CommonException {
			ArrayList<ToolSourceMessage> buildMessage = new RustBuildOutputParser() {
				@Override
				protected void handleMessageParseError(CommonException ce) {
					 LangCore.logStatus(LangCore.createCoreException(ce));
				}
			}.parseOutput(buildAllResult);
			
			ToolMarkersUtil.addErrorMarkers(buildMessage, ResourceUtils.getProjectLocation(project));
		}
	}
	
}