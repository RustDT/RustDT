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
package com.github.rustdt.ide.core.operations;

import static melnorme.utilbox.core.CoreUtil.array;
import static melnorme.utilbox.misc.StringUtil.nullAsEmpty;

import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;

import com.github.rustdt.tooling.RustBuildOutputParser;

import melnorme.lang.ide.core.BundleInfo;
import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.lang.ide.core.operations.ToolMarkersUtil;
import melnorme.lang.ide.core.operations.build.BuildManager;
import melnorme.lang.ide.core.operations.build.BuildTarget;
import melnorme.lang.ide.core.operations.build.CommonBuildTargetOperation;
import melnorme.lang.ide.core.operations.build.ValidatedBuildTarget;
import melnorme.lang.ide.core.project_model.LangBundleModel;
import melnorme.lang.ide.core.project_model.ProjectBuildInfo;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.bundle.BuildTargetNameParser;
import melnorme.lang.tooling.bundle.FileRef;
import melnorme.lang.tooling.ops.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Rust builder, using Cargo.
 */
public class RustBuildManager extends BuildManager {
	
	public static final String BuildType_Default = "build";
	
	public RustBuildManager(LangBundleModel bundleModel) {
		super(bundleModel);
	}
	
	@Override
	protected ArrayList2<BuildTarget> createBuildTargetsForNewInfo(BundleInfo newBundleInfo,
			ProjectBuildInfo currentBuildInfo) {
		
		ArrayList2<BuildTarget> buildTargets = new ArrayList2<>();
		
		buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, true, 
			getBuildTargetName2("", "crate")));
		
		buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, true, 
			getBuildTargetName2("", "tests")));
		
		for(FileRef fileRef : newBundleInfo.getManifest().getEffectiveBinaries()) {
			buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, true, 
				getBuildTargetName2(fileRef.getBinaryPathString(), "bin")));
		}
		
		return buildTargets;
	}
	
	@Override
	public BuildTargetNameParser getBuildTargetNameParser() {
		return new BuildTargetNameParser() {
			@Override
			protected String getNameSeparator() {
				return ":";
			}
			
			@Override
			public String getFullName(String buildConfig, String buildType) {
				String name = buildType;
				if(buildConfig != null && !buildConfig.isEmpty()) {
					name += getNameSeparator() + buildConfig;
				}
				return name;
			}
			
			@Override
			public String getBuildConfigName(String targetName) {
				return nullAsEmpty(StringUtil.segmentAfterMatch(targetName, getNameSeparator()));
			}
			
			@Override
			public String getBuildTypeName(String targetName) {
				return StringUtil.substringUntilMatch(targetName, getNameSeparator());
			}
			
		};
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
					throw new CommonException("No executable available.");
				}
			},
			new RustBuildType("bin") {
				@Override
				public String getDefaultBuildOptions(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					/* FIXME: Validate getBuildConfigName*/
					return "build --bin " + validatedBuildTarget.getBuildConfigName();
				}
				
				@Override
				public String getArtifactPath(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return "target/debug/" + validatedBuildTarget.getBuildConfigName();
				}
			},
			new RustBuildType("tests") {
				@Override
				public String getDefaultBuildOptions(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return "test --no-run";
				}
				
				@Override
				public String getArtifactPath(ValidatedBuildTarget validatedBuildTarget) throws CommonException {
					return super.getArtifactPath(validatedBuildTarget);
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