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

import static melnorme.utilbox.core.CoreUtil.areEqual;

import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.github.rustdt.tooling.RustBuildOutputParser;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.lang.ide.core.operations.ToolMarkersUtil;
import melnorme.lang.ide.core.operations.build.BuildManager;
import melnorme.lang.ide.core.operations.build.BuildTarget;
import melnorme.lang.ide.core.operations.build.BuildTargetRunner;
import melnorme.lang.ide.core.operations.build.BuildTargetRunner.BuildConfiguration;
import melnorme.lang.ide.core.operations.build.BuildTargetRunner.BuildType;
import melnorme.lang.ide.core.operations.build.CommonBuildTargetOperation;
import melnorme.lang.ide.core.project_model.AbstractBundleInfo;
import melnorme.lang.ide.core.project_model.LangBundleModel;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.ops.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Rust builder, using Cargo.
 */
public class RustBuildManager extends BuildManager {
	
	public static final String BuildType_Default = "<default>";
	
	public RustBuildManager(LangBundleModel<? extends AbstractBundleInfo> bundleModel) {
		super(bundleModel);
	}
	
	@Override
	protected Indexable<BuildType> getBuildTypes_do() {
		return ArrayList2.create(
			new RustBuildType(BuildType_Default),
			new RustBuildType("test")
		);
	}
	
	protected class RustBuildType extends BuildType {
		public RustBuildType(String name) {
			super(name);
		}
		
		@Override
		public String getDefaultBuildOptions(BuildTargetRunner buildTargetRunner) throws CommonException {
			return "";
		}
		
	}
	
	@Override
	public BuildTargetRunner createBuildTargetOperation(IProject project, BuildConfiguration buildConfig,
			String buildTypeName, BuildTarget buildSettings) {
		return new BuildTargetRunner(project, buildConfig, buildTypeName, buildSettings.getBuildOptions()) {
			
			@Override
			public CommonBuildTargetOperation getBuildOperation(OperationInfo parentOpInfo, Path buildToolPath,
					boolean fullBuild) {
				return new RustBuildTargetOperation(parentOpInfo, project, buildToolPath, this, fullBuild);
			}
		};
	}
	
	/* ----------------- Build ----------------- */
	
	protected class RustBuildTargetOperation extends CommonBuildTargetOperation {
		
		public RustBuildTargetOperation(OperationInfo parentOpInfo, IProject project,
				Path buildToolPath, BuildTargetRunner buildTargetOp, boolean fullBuild) {
			super(buildTargetOp.getBuildManager(), parentOpInfo, project, buildToolPath, buildTargetOp, fullBuild);
		}
		
		@Override
		protected void addToolCommand(ArrayList2<String> commands)
				throws CoreException, CommonException, OperationCancellation {
			//super.addToolCommand(commands);
		}
		
		@Override
		protected void addMainArguments(ArrayList2<String> commands) throws CommonException {
			String buildType = buildTarget.getBuildTypeName();
			if(buildType.isEmpty() || areEqual(buildType, BuildType_Default)) {
				commands.add("build");
			}
			else if(areEqual(buildType, "test")) {
				// TODO: properly implement other test targets
				commands.addElements("test", "--no-run");
			} else {
				throw CommonException.fromMsgFormat("Unknown build type `{0}`.", buildType);
			}
		}
		
		@Override
		protected ProcessBuilder getProcessBuilder(ArrayList2<String> commands)
				throws CommonException, OperationCancellation, CoreException {
			Location projectLocation = ResourceUtils.getProjectLocation(getProject());
			return getToolManager().createToolProcessBuilder(getBuildToolPath(), projectLocation, 
				commands.toArray(String.class));
		}
		
		@Override
		protected void processBuildOutput(ExternalProcessResult processResult) throws CoreException, CommonException {
			ArrayList<ToolSourceMessage> buildMessage = new RustBuildOutputParser() {
				@Override
				protected void handleMessageParseError(CommonException ce) {
					 LangCore.logStatus(LangCore.createCoreException(ce));
				}
			}.parseOutput(processResult);
			
			ToolMarkersUtil.addErrorMarkers(buildMessage, ResourceUtils.getProjectLocation(project));
		}
	}
	
}