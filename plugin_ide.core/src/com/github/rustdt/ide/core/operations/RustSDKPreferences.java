package com.github.rustdt.ide.core.operations;

import melnorme.lang.ide.core.operations.ToolchainPreferences;
import melnorme.lang.ide.core.utils.prefs.StringPreference;
import melnorme.lang.tooling.data.AbstractValidator.ValidationException;
import melnorme.lang.tooling.data.LocationValidator;
import melnorme.utilbox.misc.Location;

public interface RustSDKPreferences extends ToolchainPreferences {
	
	public static class RustSDKAcessor {
		public Location getSDKLocation() throws ValidationException {
			return new LocationValidator("Rust installation:").getValidatedLocation(SDK_PATH.get());
		}
	}
	
	public static final RustSDKAcessor SDK_PATH_Acessor = new RustSDKAcessor();
	
	public static final StringPreference SDK_SRC_PATH = new StringPreference("sdk_src_path", "");
	
}