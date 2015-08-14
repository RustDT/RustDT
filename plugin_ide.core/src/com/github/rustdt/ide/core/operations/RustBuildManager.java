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

import static melnorme.utilbox.core.CoreUtil.array;

import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;

import com.github.rustdt.tooling.RustBuildOutputParser;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.lang.ide.core.operations.ToolMarkersUtil;
import melnorme.lang.ide.core.operations.build.BuildManager;
import melnorme.lang.ide.core.operations.build.CommonBuildTargetOperation;
import melnorme.lang.ide.core.operations.build.ValidatedBuildTarget;
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
	
	public static final String BuildType_Default = "dev";
	
	public RustBuildManager(LangBundleModel<? extends AbstractBundleInfo> bundleModel) {
		super(bundleModel);
	}
	
	protected abstract class RustBuildType extends BuildType {
		public RustBuildType(String name) {
			super(name);
		}
		
		@Override
		public CommonBuildTargetOperation getBuildOperation(ValidatedBuildTarget validatedBuildTarget,
				OperationInfo opInfo, Path buildToolPath) throws CommonException, CoreException {
			return new RustBuildTargetOperation(validatedBuildTarget, opInfo, buildToolPath);
		}
		
	}
	
	@Override
	protected Indexable<BuildType> getBuildTypes_do() {
		return ArrayList2.create(
			new RustBuildType(BuildType_Default) {
				@Override
				public String getDefaultBuildOptions(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return "build";
				}
				
				@Override
				public String getArtifactPath(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return "target/debug/" + validatedBuildTarget.getBuildConfigName();
				}
			},
			new RustBuildType("test") {
				@Override
				public String getDefaultBuildOptions(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return "test --no-run";
				}
				
				@Override
				public String getArtifactPath(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return super.getArtifactPath(validatedBuildTarget);
				}
			},
			new RustBuildType("release") {
				@Override
				public String getDefaultBuildOptions(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return "build --release";
				};
				
				@Override
				public String getArtifactPath(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return "target/release/" + validatedBuildTarget.getBuildConfigName();
				}
			}
		);
	}
	
	/* ----------------- Build ----------------- */
	
	protected class RustBuildTargetOperation extends CommonBuildTargetOperation {
		
		public RustBuildTargetOperation(ValidatedBuildTarget validatedBuildTarget, OperationInfo parentOpInfo, 
				Path buildToolPath) throws CommonException, CoreException {
			super(validatedBuildTarget.buildMgr, validatedBuildTarget, parentOpInfo, buildToolPath);
		}
		
		@Override
		protected void addToolCommand(ArrayList2<String> commands)
				throws CoreException, CommonException, OperationCancellation {
			//super.addToolCommand(commands);
		}
		
		@Override
		protected String[] getMainArguments() throws CoreException, CommonException, OperationCancellation {
			return array();
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
			
			new ToolMarkersUtil().addErrorMarkers(buildMessage, ResourceUtils.getProjectLocation(project));
		}
	}
	
}