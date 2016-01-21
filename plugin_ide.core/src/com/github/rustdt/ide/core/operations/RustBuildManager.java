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
import org.eclipse.core.runtime.IProgressMonitor;

import com.github.rustdt.tooling.RustBuildOutputParser;
import com.github.rustdt.tooling.cargo.CargoManifest;

import melnorme.lang.ide.core.BundleInfo;
import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.IOperationConsoleHandler;
import melnorme.lang.ide.core.operations.ToolMarkersHelper;
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
import melnorme.utilbox.misc.CollectionUtil;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Rust builder, using Cargo.
 */
public class RustBuildManager extends BuildManager {
	
	public static final String BuildType_Default = "crate#no-tests";
	public static final String BuildType_CrateTests = "crate#tests";
	
	public RustBuildManager(LangBundleModel bundleModel) {
		super(bundleModel);
	}
	
	@Override
	protected Indexable<BuildType> getBuildTypes_do() {
		return ArrayList2.create(
			new RustCrateNoTestsBuildType(BuildType_Default),
			new RustCrateTestsBuildType(BuildType_CrateTests),
			new RustCrateBinaryBuildType("bin"),
			new RustCrateSingleTestBuildType("test")
		);
	}
	
	@Override
	protected ArrayList2<BuildTarget> createBuildTargetsForNewInfo(IProject project, BundleInfo newBundleInfo,
			ProjectBuildInfo currentBuildInfo) {
		
		ArrayList2<BuildTarget> buildTargets = new ArrayList2<>();
		
		buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, true, 
			getBuildTargetName2("", BuildType_Default)));
		
		buildTargets.add(createBuildTargetFromConfig(currentBuildInfo, true, 
			getBuildTargetName2("", BuildType_CrateTests)));
		
		return buildTargets;
	}
	
	@Override
	public BuildTargetNameParser getBuildTargetNameParser() {
		return new BuildTargetNameParser2();
	}
	
	public abstract class RustBuildType extends BuildType {
		
		protected final CoreCargoTargetHelper cargoTargetHelper = new CoreCargoTargetHelper();
		
		public RustBuildType(String name) {
			super(name);
		}
		
		@Override
		protected BuildConfiguration getValidBuildconfiguration(String buildConfigName,
				ProjectBuildInfo buildInfo) throws CommonException {
			return new BuildConfiguration(buildConfigName, null);
		}
		
		protected Location getProjectLocation(ValidatedBuildTarget vbt) throws CommonException {
			return ResourceUtils.getProjectLocation2(vbt.getProject());
		}
		
		protected LaunchArtifact getLaunchArtifact(ValidatedBuildTarget vbt, FileRef fileRef) throws CommonException {
			String cargoTargetName = fileRef.getBinaryPathString();
			return getLaunchArtifact(vbt, cargoTargetName);
		}
		
		protected LaunchArtifact getLaunchArtifact(ValidatedBuildTarget vbt, String cargoTargetName)
				throws CommonException {
			String exePath = cargoTargetHelper.getExecutablePathForCargoTarget(cargoTargetName, vbt);
			return new LaunchArtifact(cargoTargetName, exePath);
		}
		
		@Override
		public CommonBuildTargetOperation getBuildOperation(ValidatedBuildTarget validatedBuildTarget,
				IOperationConsoleHandler opHandler, Path buildToolPath) throws CommonException, CoreException {
			return new RustBuildTargetOperation(validatedBuildTarget, opHandler, buildToolPath);
		}
		
	}
	
	public class RustCrateNoTestsBuildType extends RustBuildType {
		
		public RustCrateNoTestsBuildType(String name) {
			super(name);
		}
		
		@Override
		protected void getDefaultBuildOptions(ValidatedBuildTarget vbt, ArrayList2<String> buildArgs) {
			buildArgs.add("build");
		}
		
		@Override
		public LaunchArtifact getMainLaunchArtifact(ValidatedBuildTarget vbt) throws CommonException {
			CargoManifest manifest = vbt.getBundleInfo().getManifest();
			
			Collection2<FileRef> effectiveBinaries = manifest.getEffectiveBinaries(getProjectLocation(vbt));
			if(effectiveBinaries.size() == 1) {
				return getLaunchArtifact(vbt, CollectionUtil.getSingleElementOrNull(effectiveBinaries));
			}
			return null;
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(ValidatedBuildTarget vbt) throws CommonException {
			CargoManifest manifest = vbt.getBundleInfo().getManifest();
			
			ArrayList2<LaunchArtifact> binariesPaths = new ArrayList2<>();
			
			for(FileRef binTargetName : manifest.getEffectiveBinaries(getProjectLocation(vbt))) {
				binariesPaths.add(getLaunchArtifact(vbt, binTargetName.getBinaryPathString()));
			}
			
			return binariesPaths;
		}
		
	}
	
	public class RustCrateTestsBuildType extends RustBuildType {
		
		public RustCrateTestsBuildType(String name) {
			super(name);
		}
		
		@Override
		protected void getDefaultBuildOptions(ValidatedBuildTarget vbt, ArrayList2<String> buildArgs) {
			buildArgs.addElements("test", "--no-run");
		}
		
		@Override
		public LaunchArtifact getMainLaunchArtifact(ValidatedBuildTarget vbt) throws CommonException {
			return null;
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(ValidatedBuildTarget vbt) throws CommonException {
			CargoManifest manifest = vbt.getBundleInfo().getManifest();
			
			return addTestsSubTargets(vbt, manifest, new ArrayList2<>());
		}
		
		protected ArrayList2<LaunchArtifact> addTestsSubTargets(ValidatedBuildTarget vbt, CargoManifest manifest,
				ArrayList2<LaunchArtifact> launchArtifacts) throws CommonException {
			for(String testTargetName : manifest.getEffectiveTestTargets(getProjectLocation(vbt))) {
				launchArtifacts.add(cargoTargetHelper.getLaunchArtifactForTestTarget(vbt, testTargetName));
			}
			return launchArtifacts;
		}
		
	}
	
	// Not used a the moment
	public class RustCrateBinaryBuildType extends RustBuildType {
		
		public RustCrateBinaryBuildType(String name) {
			super(name);
		}
		
		@Override
		protected void getDefaultBuildOptions(ValidatedBuildTarget vbt, ArrayList2<String> buildArgs) {
			buildArgs.addElements("build", "--bin", vbt.getBuildConfigName());
		}
		
		@Override
		public LaunchArtifact getMainLaunchArtifact(ValidatedBuildTarget vbt) throws CommonException {
			return getLaunchArtifact(vbt, vbt.getBuildConfigName());
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(ValidatedBuildTarget vbt) throws CommonException {
			return null;
		}
	}
	
	// Not used a the moment
	public class RustCrateSingleTestBuildType extends RustBuildType {
		
		public RustCrateSingleTestBuildType(String name) {
			super(name);
		}
		
		@Override
		protected void getDefaultBuildOptions(ValidatedBuildTarget vbt, ArrayList2<String> buildArgs) {
			String testName = vbt.getBuildConfigName();
			buildArgs.addElements("build", "--test", testName);
		}
		
		@Override
		public LaunchArtifact getMainLaunchArtifact(ValidatedBuildTarget vbt) throws CommonException {
			String testTargetName = vbt.getBuildConfigName();
			return cargoTargetHelper.getLaunchArtifactForTestTarget(vbt, testTargetName);
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(ValidatedBuildTarget vbt) throws CommonException {
			return null;
		}
		
	}

	/* ----------------- Build ----------------- */
	
	protected class RustBuildTargetOperation extends CommonBuildTargetOperation {
		
		public RustBuildTargetOperation(ValidatedBuildTarget validatedBuildTarget, IOperationConsoleHandler opHandler, 
				Path buildToolPath) throws CommonException, CoreException {
			super(validatedBuildTarget.buildMgr, validatedBuildTarget, opHandler, buildToolPath);
		}
		
		@Override
		protected void processBuildOutput(ExternalProcessResult processResult, IProgressMonitor pm) 
				throws CoreException, CommonException {
			ArrayList<ToolSourceMessage> buildMessage = new RustBuildOutputParser() {
				@Override
				protected void handleParseError(CommonException ce) {
					 LangCore.logStatus(LangCore.createCoreException(ce));
				}
			}.parseOutput(processResult);
			
			new ToolMarkersHelper().addErrorMarkers(buildMessage, ResourceUtils.getProjectLocation(project), pm);
		}
	}
	
}