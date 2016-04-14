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
package com.github.rustdt.ide.ui.preferences;

import java.nio.file.Path;

import melnorme.lang.ide.ui.operations.StartToolDownload_FromField;
import melnorme.lang.ide.ui.preferences.pages.DownloadToolTextField;
import melnorme.lang.utils.EnvUtils;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.MiscUtil;
import melnorme.utilbox.misc.PathUtil;

public class Start_CargoInstallJob_Operation extends StartToolDownload_FromField {
	
	public static Indexable<String> dlArgs(String dlSource, String gitTag) {
		ArrayList2<String> args = new ArrayList2<>();
		args.addElements("--git", dlSource);
		if(gitTag != null) {
			args.addElements("--tag", gitTag);
		}
		return args;
	}
	
	/* -----------------  ----------------- */
	
	protected final ArrayList2<String> sourceArgs = new ArrayList2<>();

	public Start_CargoInstallJob_Operation(String crateName, DownloadToolTextField downloadToolTextField,
			Indexable<String> sourceArgs, String exeName) {
		super(
			"Download " + crateName, 
			"Downloading " + crateName + "...", 
			downloadToolTextField, "", exeName);
		
		this.sourceArgs.addAll2(sourceArgs);
	}
	
	protected String getSDKPath() {
		return toolMgr.getSDKPathPreference(null);
	}
	
	@Override
	protected ProcessBuilder getProcessToStart_andSetToolPath() throws CommonException {
		Path sdkToolPath = toolMgr.getSDKToolPathValidator().getValidatedPath(getSDKPath());
		
		// Determine install destination path
		Location dest = getCargoRootDestinationPath();
		
		toolBinPath = dest.resolve(getBinExeSuffix()).toPathString();
		
		ArrayList2<String> args = new ArrayList2<>("install");
		args.addAll(sourceArgs);
		args.addElements("--root", dest.toPathString());
		
		/* FIXME: re-test*/
		return toolMgr.createToolProcessBuilder(null, sdkToolPath, args.toArray(String.class));
	}
	
	protected Path getBinExeSuffix() {
		return PathUtil.createValidPath("bin").resolve(exeName + MiscUtil.getExecutableSuffix());
	}
	
	protected Location getCargoRootDestinationPath() throws CommonException {
		Path currentPath = PathUtil.createPath(toolField.get());
		if(
			currentPath.isAbsolute() &&
			currentPath.endsWith(getBinExeSuffix())
		) {
			// Reuse path from prefs field 
			return Location.create(currentPath).getParent().getParent();
		}
		return getHomeDir().resolve_fromValid(".cargo/RustDT");
	}
	
	protected Location getHomeDir() throws CommonException {
		if(MiscUtil.OS_IS_WINDOWS) {
			return EnvUtils.getLocationFromEnvVar("UserProfile");
		}
		return EnvUtils.getLocationFromEnvVar("HOME");
	}
	
}