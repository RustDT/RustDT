package com.github.rustdt.ide.core.operations;

import melnorme.lang.ide.core.bundlemodel.SDKPreferences;
import melnorme.lang.ide.core.utils.prefs.StringPreference;

public interface RustSDKPreferences extends SDKPreferences {
	
	public static final StringPreference SDK_SRC_PATH = new StringPreference("sdk_src_path", "");
	
}