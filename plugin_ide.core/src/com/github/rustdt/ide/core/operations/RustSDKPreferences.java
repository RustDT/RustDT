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

import static melnorme.utilbox.core.CoreUtil.option;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;

import com.github.rustdt.tooling.ops.RacerOperation.RustRacerLocationValidator;
import com.github.rustdt.tooling.ops.RustSDKLocationValidator;
import com.github.rustdt.tooling.ops.RustSDKSrcLocationValidator;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ToolchainPreferences;
import melnorme.lang.ide.core.utils.prefs.DerivedValuePreference;
import melnorme.lang.tooling.data.ValidationException;
import melnorme.lang.tooling.ops.util.LocationOrSinglePathValidator;
import melnorme.utilbox.misc.Location;

public interface RustSDKPreferences extends ToolchainPreferences {
	
	public static class RustSDKAcessor {
		public Location getSDKLocation(IProject project) throws ValidationException {
			return new RustSDKLocationValidator().getRootLocation(
				SDK_PATH.getEffectiveValue(option(project)));
		}
	}
	
	public static final RustSDKAcessor SDK_PATH_Acessor = new RustSDKAcessor();
	
	public static final DerivedValuePreference<Location> SDK_SRC_PATH3 = new DerivedValuePreference<>(LangCore.PLUGIN_ID, 
			"sdk_src_path", "", ToolchainPreferences.USE_PROJECT_SETTINGS,
		new RustSDKSrcLocationValidator().asLocationValidator());
	
	public static final DerivedValuePreference<Path> RACER_PATH = new DerivedValuePreference<>(LangCore.PLUGIN_ID, 
		"racer_path", "", ToolchainPreferences.USE_PROJECT_SETTINGS,
		new RustRacerLocationValidator());
	
	public static final DerivedValuePreference<Path> RAINICORN_PATH2 = new DerivedValuePreference<>(LangCore.PLUGIN_ID, 
		"rainicorn_path", "", ToolchainPreferences.USE_PROJECT_SETTINGS,
		new LocationOrSinglePathValidator("Rainicorn parse_describe:"));
	
	
	public static final DerivedValuePreference<Path> RUSTFMT_PATH = new DerivedValuePreference<>(LangCore.PLUGIN_ID, 
			"rustfmt_path", "", ToolchainPreferences.USE_PROJECT_SETTINGS,
		new LocationOrSinglePathValidator("rustfmt:"));

}