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

import org.eclipse.core.resources.IProject;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ToolchainPreferences;
import melnorme.lang.ide.core.utils.prefs.IProjectPreference;
import melnorme.lang.ide.core.utils.prefs.StringPreference;
import melnorme.lang.tooling.data.ValidationException;
import melnorme.lang.tooling.ops.util.LocationValidator;
import melnorme.utilbox.misc.Location;

public interface RustSDKPreferences extends ToolchainPreferences {
	
	public static class RustSDKAcessor {
		public Location getSDKLocation(IProject project) throws ValidationException {
			return new LocationValidator("Rust installation:").getValidatedLocation(
				SDK_PATH2.getEffectiveValue(project));
		}
	}
	
	public static final RustSDKAcessor SDK_PATH_Acessor = new RustSDKAcessor();
	
	public static final IProjectPreference<String> SDK_SRC_PATH2 = new StringPreference(LangCore.PLUGIN_ID, 
		"sdk_src_path", "", ToolchainPreferences.USE_PROJECT_SETTINGS)
			.getProjectPreference();
	
	public static final IProjectPreference<String> RACER_PATH = new StringPreference(LangCore.PLUGIN_ID, 
		"racer_path", "", ToolchainPreferences.USE_PROJECT_SETTINGS).getProjectPreference();
	
}