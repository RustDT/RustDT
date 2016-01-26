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
		Location dest = EnvUtils.getLocationFromEnvVar("HOME").resolve_fromValid(".cargo/rustdt");
		
		toolBinPath = dest.resolve("bin").resolve(exeName).toPathString() + MiscUtil.getExecutableSuffix();
		
		ArrayList2<String> args = new ArrayList2<>("install");
		args.addAll(sourceArgs);
		args.addElements("--root", dest.toPathString());
		
		return toolMgr.createToolProcessBuilder(sdkToolPath, null, args.toArray(String.class));
	}
	
}