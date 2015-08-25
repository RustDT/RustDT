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
import melnorme.utilbox.collections.Collection2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.ArrayUtil;
import melnorme.utilbox.misc.CollectionUtil;
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
		public LaunchArtifact getMainLaunchArtifact(ValidatedBuildTarget vbt) throws CommonException {
			return CollectionUtil.getSingleElementOrNull(getSubTargetLaunchArtifacts(vbt));
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(ValidatedBuildTarget vbt) throws CommonException {
			BundleInfo bundleInfo = vbt.getBundleInfo();
			ArrayList2<LaunchArtifact> binariesPaths = new ArrayList2<>();
			
			Location projectLoc = ResourceUtils.getProjectLocation2(vbt.getProject());
			
			for(FileRef fileRef : bundleInfo.getManifest().getEffectiveBinaries(projectLoc)) {
				binariesPaths.add(getLaunchArtifact(fileRef, vbt));
			}
			
			return binariesPaths;
		}
		
		protected LaunchArtifact getLaunchArtifact(FileRef fileRef, ValidatedBuildTarget vbt) throws CommonException {
			String cargoTargetName = fileRef.getBinaryPathString();
			String exePath = getExecutablePathForCargoTarget(cargoTargetName, vbt);
			return new LaunchArtifact(cargoTargetName, exePath);
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
		public LaunchArtifact getMainLaunchArtifact(ValidatedBuildTarget vbt) throws CommonException {
			String cargoTargetName = vbt.getBuildConfigName();
			String exePath = getExecutablePathForCargoTarget(cargoTargetName, vbt);
			return new LaunchArtifact(cargoTargetName, exePath);
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(ValidatedBuildTarget vbt) throws CommonException {
			return null;
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
		public LaunchArtifact getMainLaunchArtifact(ValidatedBuildTarget vbt) throws CommonException {
			String testTargetName = vbt.getBuildConfigName();
			if(testTargetName.isEmpty()) {
				// Rust doesn't currently provide a way to run all crate tests in a single executable.
				return null;
			} else {
				return getLaunchArtifactForTestTarget(vbt, testTargetName);
			}
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(ValidatedBuildTarget vbt) throws CommonException {
			if(!vbt.getBuildConfigName().isEmpty()) {
				return null;
			}
			
			BundleInfo bundleInfo = vbt.getBundleInfo();
			ArrayList2<LaunchArtifact> binariesPaths = new ArrayList2<>();
			
			Location projectLoc = ResourceUtils.getProjectLocation2(vbt.getProject());
			Collection2<String> effectiveTestTargets = bundleInfo.getManifest().getEffectiveTestTargets(projectLoc);
			
			for(String testTargetName : effectiveTestTargets) {
				binariesPaths.add(getLaunchArtifactForTestTarget(vbt, testTargetName));
			}
			
			return binariesPaths;
		}
		
		protected LaunchArtifact getLaunchArtifactForTestTarget(ValidatedBuildTarget vbt, String testTargetName) 
				throws CommonException {
			String exePath = getExecutablePathForTestTarget(vbt, testTargetName);
			
			String artifactName = "test." + testTargetName;
			return new LaunchArtifact(artifactName, exePath);
		}
		
		protected String getExecutablePathForTestTarget(ValidatedBuildTarget vbt, String cargoTargetName) 
				throws CommonException {
			Location projectLoc = ResourceUtils.getProjectLocation2(vbt.getProject());
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