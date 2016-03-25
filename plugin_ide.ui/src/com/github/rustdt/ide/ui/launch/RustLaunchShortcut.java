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
package com.github.rustdt.ide.ui.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.github.rustdt.ide.core.operations.CoreCargoTargetHelper;
import com.github.rustdt.ide.core.operations.RustBuildManager;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.launch.BuildTargetLaunchCreator;
import melnorme.lang.ide.core.operations.build.BuildManager.BuildType;
import melnorme.lang.ide.core.operations.build.BuildTarget;
import melnorme.lang.ide.ui.launch.LangLaunchShortcut;
import melnorme.lang.ide.ui.navigator.BuildTargetsActionGroup;
import melnorme.lang.tooling.bundle.LaunchArtifact;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.StringUtil;

public class RustLaunchShortcut extends LangLaunchShortcut {
	
	protected RustBuildManager buildMgr = LangCore.getBuildManager();
	
	@Override
	protected String getLaunchTypeId() {
		return LangCore.PLUGIN_ID + ".LaunchConfigurationType";
	}
	
	@Override
	protected BuildTargetLaunchable getLaunchableForElement(Object element, IProject project,
			BuildTargetLaunchCreator launchSettings, IProgressMonitor pm)
					throws CoreException, CommonException, OperationCancellation {
		if(element instanceof IFile) {
			IFile file = (IFile) element;
			if(!file.getParent().getProjectRelativePath().equals(new Path("tests"))) {
				throw new CommonException("File is not a Cargo test source, "
						+ "it's not directly contained in the `tests` directory.");
			}
			
			String testName = StringUtil.trimEnd(file.getName(), ".rs");
			
			BuildType testsBuildType = buildMgr.getBuildType_NonNull(RustBuildManager.BuildType_CrateTests);
			BuildTarget bt = buildMgr.getValidDefinedBuildTarget(project, testsBuildType.getName(), "");
			
			LaunchArtifact launchArtifact = new CoreCargoTargetHelper().getLaunchArtifactForTestTarget(bt, testName);
			String launchNameSugestion = getLaunchNameForSubTarget(launchArtifact.getName());
			
			String buildTargetName = testsBuildType.getName();
			launchSettings = BuildTargetsActionGroup.buildTargetLaunchCreator(
				project, buildTargetName, launchArtifact.getArtifactPath(), launchNameSugestion);
			return new BuildTargetLaunchable(project, launchSettings);
		}
		
		return super.getLaunchableForElement(element, project, launchSettings, pm);
	}
	
	@Override
	public String getLaunchNameForSubTarget(String subTargetName) {
		if(subTargetName.startsWith("test.")) {
			return "test[" + StringUtil.trimStart(subTargetName, "test.")  + "]";
		}
		return "[" + subTargetName + "]";
	}
	
}