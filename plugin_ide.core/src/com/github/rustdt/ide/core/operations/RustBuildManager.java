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

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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
				getBuildTargetName2(fileRef.getBinaryPathString(), "tests")));
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
			String exeFolder = getExecutableDirectoryForCargoTarget(validatedBuildTarget);
			return exeFolder + "/" + cargoTargetName + MiscUtil.getExecutableSuffix();
		}
		
		protected String getExecutableDirectoryForCargoTarget(ValidatedBuildTarget vbt) throws CommonException {
			String profile = "debug";
			
			String[] buildArgs = vbt.getEffectiveEvaluatedBuildArguments();
			if(ArrayUtil.contains(buildArgs, "--release")) {
				profile = "release";
			}
			return "target/" + profile;
		}
		
	}
	
	public class RustCrateBuildType extends RustBuildType {
		
		public RustCrateBuildType(String name) {
			super(name);
		}
		
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
	}
	
	public class RustCrateBinaryBuildType extends RustCrateBuildType {
		
		public RustCrateBinaryBuildType(String name) {
			super(name);
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

	public class RustTestsBuildType extends RustCrateBuildType {
		
		public RustTestsBuildType(String name) {
			super(name);
		}
		
		@Override
		protected void getDefaultBuildOptions(ValidatedBuildTarget vbt, ArrayList2<String> buildArgs) {
			String testName = vbt.getBuildConfigName();
			if(testName.isEmpty()) {
				buildArgs.addElements("test", "--no-run");
			} else {
				buildArgs.addElements("build", "--test", testName);
			}
		}
		
		@Override
		public Indexable<LaunchArtifact> getLaunchArtifacts_do(ValidatedBuildTarget vbt) throws CommonException {
			BundleInfo bundleInfo = vbt.getBundleInfo();
			ArrayList2<LaunchArtifact> binariesPaths = new ArrayList2<>();
			
			Location projectLoc = ResourceUtils.getProjectLocation2(vbt.getProject());
			
			for(FileRef fileRef : bundleInfo.getManifest().getEffectiveTestBinaries(projectLoc)) {
				String cargoTargetName = fileRef.getBinaryPathString();
				String exePath = getExecutablePathForTestTarget(vbt, projectLoc, cargoTargetName);
				
				/* FIXME: need to review for lib and bin test targets*/
				String artifactName = "test." + cargoTargetName;
				binariesPaths.add(new LaunchArtifact(artifactName, exePath));
			}
			
			return binariesPaths;
		}
		
		protected String getExecutablePathForTestTarget(ValidatedBuildTarget vbt, Location projectLoc, 
				String cargoTargetName) throws CommonException {
			String exeDirectory = getExecutableDirectoryForCargoTarget(vbt);
			
			String[] matchingExes = projectLoc.resolve(exeDirectory).toFile().list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(cargoTargetName + "-");
				}
			});
			
			String testFilename;
			if(matchingExes == null || matchingExes.length == 0) {
				testFilename = "<Error: could not determine executable file for test target>";
			} else if(matchingExes.length > 1) {
				testFilename = "<Error: found multiple executable files for test target>";
			} else {
				testFilename = matchingExes[0];
			}
			
			return exeDirectory + "/" + testFilename;
		}
	}

	@Override
	protected Indexable<BuildType> getBuildTypes_do() {
		return ArrayList2.create(
			new RustCrateBuildType(BuildType_Default),
			new RustTestsBuildType("tests"),
			new RustTestsBuildType("test"),
			new RustCrateBinaryBuildType("bin")
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
		protected void processBuildOutput(ExternalProcessResult processResult, IProgressMonitor pm) 
				throws CoreException, CommonException {
			ArrayList<ToolSourceMessage> buildMessage = new RustBuildOutputParser() {
				@Override
				protected void handleMessageParseError(CommonException ce) {
					 LangCore.logStatus(LangCore.createCoreException(ce));
				}
			}.parseOutput(processResult);
			
			new ToolMarkersUtil().addErrorMarkers(buildMessage, ResourceUtils.getProjectLocation(project), pm);
		}
	}
	
}