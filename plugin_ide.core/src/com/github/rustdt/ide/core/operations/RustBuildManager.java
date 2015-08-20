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

import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.github.rustdt.tooling.RustBuildOutputParser;

import melnorme.lang.ide.core.BundleInfo;
import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.launch.LaunchUtils;
import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.lang.ide.core.operations.ToolMarkersUtil;
import melnorme.lang.ide.core.operations.build.BuildManager;
import melnorme.lang.ide.core.operations.build.BuildTarget;
import melnorme.lang.ide.core.operations.build.CommonBuildTargetOperation;
import melnorme.lang.ide.core.operations.build.ValidatedBuildTarget;
import melnorme.lang.ide.core.project_model.LangBundleModel;
import melnorme.lang.ide.core.project_model.ProjectBuildInfo;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.bundle.BuildConfiguration;
import melnorme.lang.tooling.bundle.BuildTargetNameParser;
import melnorme.lang.tooling.bundle.BuildTargetNameParser2;
import melnorme.lang.tooling.bundle.FileRef;
import melnorme.lang.tooling.bundle.LaunchArtifact;
import melnorme.lang.tooling.ops.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.ArrayUtil;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.MiscUtil;
import melnorme.utilbox.misc.PathUtil;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Rust builder, using Cargo.
 */
public class RustBuildManager extends BuildManager {
	
	public static final String BuildType_Default = "crate";
	
	public RustBuildManager(LangBundleModel bundleModel) {
		super(bundleModel);
	}
	
	@Override
	protected ArrayList2<BuildTarget> createBuildTargetsForNewInfo(IProject project, BundleInfo newBundleInfo,
			ProjectBuildInfo currentBuildInfo) {
		
		ArrayList2<BuildTarget> buildTargets = new ArrayList2<>();
		
		buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, true, 
			getBuildTargetName2("", BuildType_Default)));
		
		buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, true, 
			getBuildTargetName2("", "tests")));
		
		Location projectLocation;
		try {
			projectLocation = ResourceUtils.getProjectLocation2(project);
		} catch(CommonException e) {
			return buildTargets;
		}
		
		for(FileRef fileRef : newBundleInfo.getManifest().getEffectiveBinaries(projectLocation)) {
			buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, false, 
				getBuildTargetName2(fileRef.getBinaryPathString(), "bin")));
		}
		
		for(FileRef fileRef : newBundleInfo.getManifest().getEffectiveTestBinaries(projectLocation)) {
			buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, false, 
				getBuildTargetName2(fileRef.getBinaryPathString(), "test")));
		}
		
		return buildTargets;
	}
	
	@Override
	public BuildTargetNameParser getBuildTargetNameParser() {
		return new BuildTargetNameParser2();
	}
	
	public abstract class RustBuildType extends BuildType {
		
		public RustBuildType(String name) {
			super(name);
		}
		
		@Override
		public CommonBuildTargetOperation getBuildOperation(ValidatedBuildTarget validatedBuildTarget,
				OperationInfo opInfo, Path buildToolPath) throws CommonException, CoreException {
			return new RustBuildTargetOperation(validatedBuildTarget, opInfo, buildToolPath);
		}
		
		protected String getExecutablePathForCargoTarget(String cargoTargetName, 
				ValidatedBuildTarget validatedBuildTarget) throws CommonException {
			String profile = "debug/";
			/* FIXME: refactor getEvaluatedAndParsedArguments */
			String[] buildArgs = LaunchUtils.getEvaluatedAndParsedArguments(validatedBuildTarget.getEffectiveBuildArguments());
			if(ArrayUtil.contains(buildArgs, "--release")) {
				profile = "release/";
			}
			return "target/" + profile + cargoTargetName + MiscUtil.getExecutableSuffix();
		}
		
	}
	
	@Override
	protected Indexable<BuildType> getBuildTypes_do() {
		return ArrayList2.create(
			new RustBuildType(BuildType_Default) {
				
				@Override
				protected BuildConfiguration getValidBuildconfiguration(String buildConfigName,
						ProjectBuildInfo buildInfo) throws CommonException {
					return new BuildConfiguration(buildConfigName, null);
				}
				
				@Override
				protected void getDefaultBuildOptions(ValidatedBuildTarget vbt, ArrayList2<String> buildArgs) {
					buildArgs.add("build");
				}
				
				@Override
				public Indexable<LaunchArtifact> getLaunchArtifacts_do(ValidatedBuildTarget vbt)
						throws CommonException {
					BundleInfo bundleInfo = vbt.getBundleInfo();
					ArrayList2<LaunchArtifact> binariesPaths = new ArrayList2<>();
					
					Location projectLoc = ResourceUtils.getProjectLocation2(vbt.getProject());
					
					for(FileRef fileRef : bundleInfo.getManifest().getEffectiveBinaries(projectLoc)) {
						String cargoTargetName = fileRef.getBinaryPathString();
						String exePath = getExecutablePathForCargoTarget(cargoTargetName, vbt);
						binariesPaths.add(new LaunchArtifact(cargoTargetName, exePath));
					}
					
					return binariesPaths;
				}
			},
			new RustBuildType("tests") {
				
				@Override
				protected BuildConfiguration getValidBuildconfiguration(String buildConfigName,
						ProjectBuildInfo buildInfo) throws CommonException {
					return new BuildConfiguration("", null);
				}
				
				@Override
				protected void getDefaultBuildOptions(ValidatedBuildTarget vbt, ArrayList2<String> buildArgs) {
					buildArgs.addElements("test", "--no-run");
				}
				
				@Override
				public Indexable<LaunchArtifact> getLaunchArtifacts_do(ValidatedBuildTarget vbt)
						throws CommonException {
					BundleInfo bundleInfo = vbt.getBundleInfo();
					ArrayList2<LaunchArtifact> binariesPaths = new ArrayList2<>();
					
					Location projectLoc = ResourceUtils.getProjectLocation2(vbt.getProject());
					
					for(FileRef fileRef : bundleInfo.getManifest().getEffectiveTestBinaries(projectLoc)) {
						String cargoTargetName = fileRef.getBinaryPathString();
						String exePath = getExecutablePathForCargoTarget(cargoTargetName, vbt);
						binariesPaths.add(new LaunchArtifact("test[" + cargoTargetName + "]", exePath));
					}
					
					return binariesPaths;
				}
				
			},
			new RustBuildType("bin") {
				
				@Override
				protected BuildConfiguration getValidBuildconfiguration(String buildConfigName,
						ProjectBuildInfo buildInfo) throws CommonException {
					
					PathUtil.createPath(buildConfigName); // Validate name
					
					return new BuildConfiguration(buildConfigName, null);
				}
				
				@Override
				protected void getDefaultBuildOptions(ValidatedBuildTarget vbt, ArrayList2<String> buildArgs) {
					buildArgs.addElements("build", "--bin", vbt.getBuildConfigName());
				}
				
				@Override
				public Indexable<LaunchArtifact> getLaunchArtifacts_do(ValidatedBuildTarget vbt) 
						throws CommonException {
					String cargoTargetName = vbt.getBuildConfigName();
					String exePath = getExecutablePathForCargoTarget(cargoTargetName, vbt);
					
					return new ArrayList2<>(
						new LaunchArtifact(cargoTargetName, exePath)
					);
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