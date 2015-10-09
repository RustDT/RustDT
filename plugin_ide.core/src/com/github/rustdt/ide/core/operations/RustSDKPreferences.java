package com.github.rustdt.ide.core.operations;

import org.eclipse.core.resources.IProject;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ToolchainPreferences;
import melnorme.lang.ide.core.utils.prefs.IProjectPreference;
import melnorme.lang.ide.core.utils.prefs.StringPreference;
import melnorme.lang.tooling.data.LocationValidator;
import melnorme.lang.tooling.data.ValidationException;
import melnorme.utilbox.misc.Location;

public interface RustSDKPreferences extends ToolchainPreferences {
	
	public static class RustSDKAcessor {
		public Location getSDKLocation(IProject project) throws ValidationException {
			return new LocationValidator("Rust installation:").getValidatedLocation(
				SDK_PATH.getProjectPreference().getEffectiveValue(project));
		}
	}
	
	public static final RustSDKAcessor SDK_PATH_Acessor = new RustSDKAcessor();
	
	public static final IProjectPreference<String> SDK_SRC_PATH2 = new StringPreference(LangCore.PLUGIN_ID, 
		"sdk_src_path", "", ToolchainPreferences.USE_PROJECT_SETTINGS)
			.getProjectPreference();
	
	public static final IProjectPreference<String> RACER_PATH = new StringPreference(LangCore.PLUGIN_ID, 
		"racer_path", "", ToolchainPreferences.USE_PROJECT_SETTINGS).getProjectPreference();
	
}