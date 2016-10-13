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
package com.github.rustdt.tooling.cargo;

import java.io.File;
import java.io.FilenameFilter;

import melnorme.lang.tooling.bundle.LaunchArtifact;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.MiscUtil;
import melnorme.utilbox.misc.StringUtil;

// XXX: This could use some cleanup. Maybe create a CargoTarget ADT?
public class CargoTargetHelper {
	
	public static enum DebugMode { YES, NO ; public boolean isTrue() { return this == YES; } }
	
	public static DebugMode getBuildMode(Indexable<String> buildArgs) {
		if(buildArgs.contains("--release")) {
			return DebugMode.NO;
		}
		return DebugMode.YES;
	}
	
	/* -----------------  ----------------- */
	
	public String getExecutablePathForCargoTarget(String cargoTargetName, DebugMode buildMode) {
		String exeDir = getExeParentDir(buildMode);
		return exeDir + "/" + cargoTargetName + MiscUtil.getExecutableSuffix();
	}
	
	public String getExeParentDir(DebugMode modeIsDebug) {
		String profile = modeIsDebug.isTrue() ? "debug" : "release";
		return "target/" + profile;
	}
	
	public LaunchArtifact getLaunchArtifactForTestTarget(Location crateLocation, String testTargetName, 
			DebugMode buildMode)
			throws CommonException {
		String cargoTargetName = "test." + testTargetName;
		String exePath = getExecutablePathForTestTarget(testTargetName, buildMode, crateLocation);
		return new LaunchArtifact(cargoTargetName, exePath);
	}
	
	public String getExecutablePathForTestTarget(String cargoTargetName, DebugMode buildMode, Location crateLocation) 
			throws CommonException {
		String exeDirectory = getExeParentDir(buildMode);
		
		cargoTargetName = StringUtil.trimStart(cargoTargetName, "lib.");
		cargoTargetName = StringUtil.trimStart(cargoTargetName, "bin.");
		String exePrefix = cargoTargetName;
		
		String[] matchingExes = crateLocation.resolve(exeDirectory).toFile().list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File path = new File(dir, name);
				return name.startsWith(exePrefix + "-") && !path.isDirectory();
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