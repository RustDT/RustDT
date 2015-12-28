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
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import static melnorme.utilbox.core.CoreUtil.array;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;

import melnorme.lang.ide.ui.operations.StartBundleDownloadOperation;
import melnorme.lang.ide.ui.preferences.pages.DownloadToolTextField;
import melnorme.lang.utils.EnvUtils;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.ArrayUtil;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.MiscUtil;

public class Start_CargoInstallJob_Operation extends StartBundleDownloadOperation {
	
	protected final DownloadToolTextField downloadToolField;
	protected final String gitSource;
	protected final String gitTag;
	protected final String exeName;

	public Start_CargoInstallJob_Operation(String operationName, 
			DownloadToolTextField downloadToolTextField,
			String gitSource, String gitTag, String exeName) {
		super(operationName);
		this.downloadToolField = assertNotNull(downloadToolTextField);
		this.gitSource = assertNotNull(gitSource);
		this.gitTag = assertNotNull(gitTag);
		this.exeName = assertNotNull(exeName);
	}
	
	protected String getSDKPath() {
		return toolMgr.getSDKPathPreference(null);
	}
	
	@Override
	protected void doOperation() throws CoreException, CommonException, OperationCancellation {
		
		Path sdkToolPath = toolMgr.getSDKToolPathValidator().getValidatedPath(getSDKPath());
		
		// Determine install destination path
		Location dest = EnvUtils.getLocationFromEnvVar("HOME").resolve_fromValid(".cargo/rustdt");
		
		String binPath = dest.resolve("bin").resolve(exeName).toPathString() + MiscUtil.getExecutableSuffix();
		downloadToolField.setFieldValue(binPath);
		
		String[] args = array("install", "--root", dest.toPathString(), "--git", gitSource);
		if(gitTag != null) {
			args = ArrayUtil.concat(args, "--tag", gitTag);
		}
		ProcessBuilder pb = toolMgr.createToolProcessBuilder(sdkToolPath, null, args);
		
		// Dialog explaining action to run
		startProcessUnderJob(pb, binPath);
	}
	
	@Override
	protected void startProcessUnderJob(ProcessBuilder pb, String cmdLineRender, String toolLocation) {
		cmdLineRender = cmdLineRender.replaceAll(Pattern.quote(" --"), " \n--");
		super.startProcessUnderJob(pb, cmdLineRender, toolLocation);
	}
	
	@Override
	protected void afterDownloadJobCompletes_inUI() {
		assertTrue(Display.getCurrent() != null);
		downloadToolField.fireFieldValueChanged();
	}
	
}