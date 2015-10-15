/*******************************************************************************
 * Copyright (c) 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.preferences;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;

import melnorme.lang.ide.ui.preferences.LangRootPreferencePage;
import melnorme.lang.ide.ui.preferences.LangSDKConfigBlock;


/**
 * The root preference page for Rust
 */
public class RustRoot__PreferencePage extends LangRootPreferencePage {
	
	public RustRoot__PreferencePage() {
	}
	
	@Override
	protected String getHelpId() {
		return null;
	}
	
	@Override
	protected LangSDKConfigBlock init_createLangSDKConfigBlock() {
		RustToolsConfigBlock langToolsBlock = new RustToolsConfigBlock();
		
		bindToPreference2(langToolsBlock.sdkSrcLocation, RustSDKPreferences.SDK_SRC_PATH2.getGlobalPreference());
		bindToPreference2(langToolsBlock.racerLocation, RustSDKPreferences.RACER_PATH.getGlobalPreference());
		
		return langToolsBlock;
	}
	
}