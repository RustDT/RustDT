package com.github.rustdt.ide.core.operations;

import org.eclipse.core.resources.IProject;

import melnorme.lang.ide.core.operations.ToolchainPreferences;
import melnorme.lang.ide.core.utils.prefs.StringPreference;
import melnorme.lang.tooling.data.LocationValidator;
import melnorme.lang.tooling.data.ValidationException;
import melnorme.utilbox.misc.Location;

public interface RustSDKPreferences extends ToolchainPreferences {
	
	public static class RustSDKAcessor {
		public Location getSDKLocation(IProject project) throws ValidationException {
			return new LocationValidator("Rust installation:").getValidatedLocation(
				SDK_PATH2.getProjectPreference().getEffectiveValue(project));
		}
	}
	
	public static final RustSDKAcessor SDK_PATH_Acessor = new RustSDKAcessor();
	
	public static final StringPreference SDK_SRC_PATH = new StringPreference("sdk_src_path", "");
	
}