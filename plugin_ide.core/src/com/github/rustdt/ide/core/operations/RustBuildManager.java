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

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.github.rustdt.tooling.RustBuildOutputParser;
import com.github.rustdt.tooling.cargo.CargoManifest;

import melnorme.lang.ide.core.BundleInfo;
import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.IOperationMonitor;
import melnorme.lang.ide.core.operations.ToolManager;
import melnorme.lang.ide.core.operations.ToolMarkersHelper;
import melnorme.lang.ide.core.operations.build.BuildManager;
import melnorme.lang.ide.core.operations.build.BuildTarget;
import melnorme.lang.ide.core.operations.build.BuildTargetData;
import melnorme.lang.ide.core.operations.build.CommandInvocation;
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
	
	public static final String BuildType_Build = "build";
	public static final String BuildType_Check = "check";
	
	public static final RustCrateBuildType BUILD_TYPE_Build = new RustCrateBuildType(BuildType_Build, "test --no-run");
	public static final RustCrateBuildType BUILD_TYPE_Check = new RustCrateBuildType(BuildType_Check, "check");
	
	public RustBuildManager(LangBundleModel bundleModel, ToolManager toolManager) {
		super(bundleModel, toolManager);
	}
	
	@Override
	protected Indexable<BuildType> getBuildTypes_do() {
		return ArrayList2.create(
			BUILD_TYPE_Build,
			BUILD_TYPE_Check
		);
	}
	
	@Override
	public BuildTargetNameParser getBuildTargetNameParser() {
		return new BuildTargetNameParser2();
	}
	
	@Override
	protected ArrayList2<BuildTarget> getDefaultBuildTargets(IProject project, BundleInfo newBundleInfo) {
		ArrayList2<BuildTarget> buildTargets = new ArrayList2<>();
		
		BuildConfiguration buildConfig = new BuildConfiguration("", null);
		buildTargets.add(createBuildTarget(project, newBundleInfo, BUILD_TYPE_Build, buildConfig, true, false));
		buildTargets.add(createBuildTarget(project, newBundleInfo, BUILD_TYPE_Check, buildConfig, false, true));
		
		return buildTargets;
	}
	
	protected BuildTarget createBuildTarget(IProject project, BundleInfo newBundleInfo, BuildType buildType,
			BuildConfiguration buildConfig, boolean normalBuildEnabled, boolean autoBuildEnabled) {
		String targetName = getBuildTargetName2(buildConfig.getName(), buildType.getName());
		
		BuildTargetData newBuildTargetData = new BuildTargetData(targetName, normalBuildEnabled, autoBuildEnabled); 
		return new BuildTarget(project, newBundleInfo, newBuildTargetData, buildType, buildConfig);
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
		public CommonBuildTargetOperation getBuildOperation(IOperationMonitor opMonitor,
				ToolManager toolMgr, BuildTarget bt, CommandInvocation buildCommand
		) throws CommonException {
			return new RustBuildTargetOperation(opMonitor, toolMgr, bt.getBuildTargetName(), buildCommand);
		}
		
	}
	
	public static class RustCrateBuildType extends RustBuildType {
		
		protected final String defaultCommandArguments;
		
		public RustCrateBuildType(String name, String defaultCommandArguments) {
			super(name);
			this.defaultCommandArguments = assertNotNull(defaultCommandArguments);
		}
		
		@Override
		public String getDefaultCommandArguments(BuildTarget bt) throws CommonException {
			return defaultCommandArguments;
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
			
			addTestsSubTargets(bt, manifest, new ArrayList2<>());
			
			return binariesPaths;
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
		
		public RustBuildTargetOperation(IOperationMonitor opMonitor, ToolManager toolManager, String buildTargetName,
				CommandInvocation buildCommand) {
			super(opMonitor, toolManager, buildTargetName, buildCommand);
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