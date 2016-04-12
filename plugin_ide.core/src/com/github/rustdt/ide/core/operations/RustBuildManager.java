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
import org.eclipse.core.runtime.IProgressMonitor;

import com.github.rustdt.tooling.RustBuildOutputParser;
import com.github.rustdt.tooling.cargo.CargoManifest;

import melnorme.lang.ide.core.BundleInfo;
import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.IOperationConsoleHandler;
import melnorme.lang.ide.core.operations.ToolMarkersHelper;
import melnorme.lang.ide.core.operations.build.BuildManager;
import melnorme.lang.ide.core.operations.build.BuildTarget;
import melnorme.lang.ide.core.operations.build.BuildTargetData;
import melnorme.lang.ide.core.operations.build.CommonBuildTargetOperation;
import melnorme.lang.ide.core.project_model.LangBundleModel;
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
import melnorme.utilbox.concurrency.OperationCancellation;
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
	
	public static final RustCrateNoTestsBuildType BUILD_TYPE_Default = 
			new RustCrateNoTestsBuildType(BuildType_Default);
	public static final RustCrateTestsBuildType BUILD_TYPE_Tests = 
			new RustCrateTestsBuildType(BuildType_CrateTests);
	
	public RustBuildManager(LangBundleModel bundleModel) {
		super(bundleModel);
	}
	
	@Override
	protected Indexable<BuildType> getBuildTypes_do() {
		return ArrayList2.create(
			BUILD_TYPE_Default,
			BUILD_TYPE_Tests
		);
	}
	
	@Override
	protected ArrayList2<BuildTarget> getDefaultBuildTargets(IProject project, BundleInfo newBundleInfo) {
		ArrayList2<BuildTarget> buildTargets = new ArrayList2<>();
		
		buildTargets.add(createBuildTarget(project, newBundleInfo, BUILD_TYPE_Default, new BuildConfiguration("", null)));
		buildTargets.add(createBuildTarget(project, newBundleInfo, BUILD_TYPE_Tests, new BuildConfiguration("", null)));
		
		return buildTargets;
	}
	
	protected BuildTarget createBuildTarget(IProject project, BundleInfo newBundleInfo, BuildType buildType,
			BuildConfiguration buildConfig) {
		String targetName = getBuildTargetName2(buildConfig.getName(), buildType.getName());
		
		BuildTargetData newBuildTargetData = new BuildTargetData(targetName, true, false); 
		
		return new BuildTarget(project, newBundleInfo, newBuildTargetData, buildType, buildConfig);
	}
	
	@Override
	public BuildTargetNameParser getBuildTargetNameParser() {
		return new BuildTargetNameParser2();
	}
	
	public static abstract class RustBuildType extends BuildType {
		
		protected final CoreCargoTargetHelper cargoTargetHelper = new CoreCargoTargetHelper();
		
		public RustBuildType(String name) {
			super(name);
		}
		
		@Override
		protected BuildConfiguration getValidBuildconfiguration(String buildConfigName, BundleInfo bundleInfo)
				throws CommonException {
			return new BuildConfiguration(buildConfigName, null);
		}
		
		protected Location getProjectLocation(BuildTarget bt) throws CommonException {
			return ResourceUtils.getProjectLocation2(bt.getProject());
		}
		
		protected LaunchArtifact getLaunchArtifact(BuildTarget bt, FileRef fileRef) throws CommonException {
			String cargoTargetName = fileRef.getBinaryPathString();
			return getLaunchArtifact(bt, cargoTargetName);
		}
		
		protected LaunchArtifact getLaunchArtifact(BuildTarget bt, String cargoTargetName)
				throws CommonException {
			String exePath = cargoTargetHelper.getExecutablePathForCargoTarget(cargoTargetName, bt);
			return new LaunchArtifact(cargoTargetName, exePath);
		}
		
		@Override
		public CommonBuildTargetOperation getBuildOperation(BuildTarget buildTarget,
				IOperationConsoleHandler opHandler, Path buildToolPath, String buildArguments) throws CommonException {
			return new RustBuildTargetOperation(buildTarget, opHandler, buildToolPath, buildArguments);
		}
		
	}
	
	public static class RustCrateNoTestsBuildType extends RustBuildType {
		
		public RustCrateNoTestsBuildType(String name) {
			super(name);
		}
		
		@Override
		public String getDefaultBuildArguments(BuildTarget bt) throws CommonException {
			return "build";
		}
		
		@Override
		public LaunchArtifact getMainLaunchArtifact(BuildTarget bt) throws CommonException {
			CargoManifest manifest = bt.getBundleInfo().getManifest();
			
			Collection2<FileRef> effectiveBinaries = manifest.getEffectiveBinaries(getProjectLocation(bt));
			if(effectiveBinaries.size() == 1) {
				return getLaunchArtifact(bt, CollectionUtil.getSingleElementOrNull(effectiveBinaries));
			}
			return null;
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(BuildTarget bt) throws CommonException {
			CargoManifest manifest = bt.getBundleInfo().getManifest();
			
			ArrayList2<LaunchArtifact> binariesPaths = new ArrayList2<>();
			
			for(FileRef binTargetName : manifest.getEffectiveBinaries(getProjectLocation(bt))) {
				binariesPaths.add(getLaunchArtifact(bt, binTargetName.getBinaryPathString()));
			}
			
			return binariesPaths;
		}
		
	}
	
	public static class RustCrateTestsBuildType extends RustBuildType {
		
		public RustCrateTestsBuildType(String name) {
			super(name);
		}
		
		@Override
		public String getDefaultBuildArguments(BuildTarget bt) throws CommonException {
			return "test --no-run";
		}
		
		@Override
		public LaunchArtifact getMainLaunchArtifact(BuildTarget bt) throws CommonException {
			return null;
		}
		
		@Override
		public Indexable<LaunchArtifact> getSubTargetLaunchArtifacts(BuildTarget bt) throws CommonException {
			CargoManifest manifest = bt.getBundleInfo().getManifest();
			
			return addTestsSubTargets(bt, manifest, new ArrayList2<>());
		}
		
		protected ArrayList2<LaunchArtifact> addTestsSubTargets(BuildTarget bt, CargoManifest manifest,
				ArrayList2<LaunchArtifact> launchArtifacts) throws CommonException {
			for(String testTargetName : manifest.getEffectiveTestTargets(getProjectLocation(bt))) {
				launchArtifacts.add(cargoTargetHelper.getLaunchArtifactForTestTarget(bt, testTargetName));
			}
			return launchArtifacts;
		}
		
	}
	
	/* ----------------- Build ----------------- */
	
	protected static class RustBuildTargetOperation extends CommonBuildTargetOperation {
		
		public RustBuildTargetOperation(BuildTarget buildTarget, IOperationConsoleHandler opHandler, 
				Path buildToolPath, String buildArguments) throws CommonException {
			super(buildTarget.buildMgr, buildTarget, opHandler, buildToolPath, buildArguments);
		}
		
		@Override
		protected void processBuildOutput(ExternalProcessResult processResult, IProgressMonitor pm) 
				throws CommonException, OperationCancellation {
			ArrayList<ToolSourceMessage> buildMessage = new RustBuildOutputParser() {
				@Override
				protected void handleParseError(CommonException ce) {
					 LangCore.logStatus(LangCore.createCoreException(ce));
				}
			}.parseOutput(processResult);
			
			new ToolMarkersHelper().addErrorMarkers(buildMessage, ResourceUtils.getProjectLocation2(project), pm);
		}
	}
	
}