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

import com.github.rustdt.tooling.cargo.CargoTargetHelper;

import melnorme.lang.ide.core.operations.build.ValidatedBuildTarget;
import melnorme.lang.tooling.bundle.LaunchArtifact;
import melnorme.utilbox.core.CommonException;

public class CoreCargoTargetHelper extends CargoTargetHelper {
	
	public DebugMode getBuildMode(ValidatedBuildTarget vbt) throws CommonException {
		return getBuildMode(vbt.getEffectiveEvaluatedBuildArguments());
	}
	
	public String getExecutablePathForCargoTarget(String cargoTargetName, 
			ValidatedBuildTarget vbt) throws CommonException {
		return getExecutablePathForCargoTarget(cargoTargetName, getBuildMode(vbt));
	}
	
	public LaunchArtifact getLaunchArtifactForTestTarget(ValidatedBuildTarget vbt, String testTargetName) 
			throws CommonException {
		return getLaunchArtifactForTestTarget(vbt.getProjectLocation(), testTargetName, getBuildMode(vbt));
	}
	
}