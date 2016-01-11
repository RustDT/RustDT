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

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.CoreUtil.array;

import java.nio.file.Path;

import melnorme.lang.ide.ui.operations.StartToolDownload_FromField;
import melnorme.lang.ide.ui.preferences.pages.DownloadToolTextField;
import melnorme.lang.utils.EnvUtils;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.ArrayUtil;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.MiscUtil;

public class Start_CargoInstallJob_Operation extends StartToolDownload_FromField {
	
	protected final String gitTag;

	public Start_CargoInstallJob_Operation(String operationName, 
			DownloadToolTextField downloadToolTextField,
			String gitSource, String gitTag, String exeName) {
		super(operationName, downloadToolTextField, gitSource, exeName);
		this.gitTag = assertNotNull(gitTag);
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
		
		String[] args = array("install", "--root", dest.toPathString(), "--git", dlSource);
		if(gitTag != null) {
			args = ArrayUtil.concat(args, "--tag", gitTag);
		}
		return toolMgr.createToolProcessBuilder(sdkToolPath, null, args);
	}
	
}