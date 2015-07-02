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
import melnorme.lang.ide.core.operations.BuildOperationCreator.CommonBuildTargetOperation;
import melnorme.lang.ide.core.operations.BuildTarget;
import melnorme.lang.ide.core.operations.LangProjectBuilder;
import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.lang.ide.core.project_model.BuildManager;
import melnorme.lang.ide.core.project_model.BundleManifestResourceListener;
import melnorme.lang.ide.core.utils.EclipseUtils;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.ops.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RustBuildManager extends BuildManager {
	
	@Override
	protected BundleManifestResourceListener init_createResourceListener() {
		return new ManagerResourceListener(new org.eclipse.core.runtime.Path("Cargo.toml"));
	}
	
	@Override
	protected BuildTarget createBuildTarget(boolean enabled, String targetName) {
		/* FIXME: */
		return new BuildTarget(enabled, targetName) {
			@Override
			public CommonBuildTargetOperation newBuildTargetOperation(OperationInfo parentOpInfo, IProject project,
					boolean fullBuild) throws CommonException {
				Path buildToolPath = getSDKToolPath();
				return new RustRunBuildOperationExtension(parentOpInfo, project, buildToolPath, this, fullBuild);
			}
		};
	}
	
	/* ----------------- Build ----------------- */
	
	protected class RustRunBuildOperationExtension extends CommonBuildTargetOperation {
		
		protected final IProject project;
		
		public RustRunBuildOperationExtension(OperationInfo parentOpInfo, IProject project,
				Path buildToolPath, BuildTarget buildTarget, boolean fullBuild) {
			super(parentOpInfo, buildToolPath, buildTarget);
			this.project = project;
			/* FIXME: */
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
			
			ProcessBuilder pb = createSDKProcessBuilder(buildCommands.toArray(String.class));
			
			ExternalProcessResult buildAllResult = runBuildTool_2(pm, pb);
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
			
			addErrorMarkers(buildMessage, ResourceUtils.getProjectLocation(project));
		}
	}
	
}