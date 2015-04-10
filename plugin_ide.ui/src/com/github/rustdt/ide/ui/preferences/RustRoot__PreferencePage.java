/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.preferences;

import melnorme.lang.ide.core.bundlemodel.SDKPreferences;
import melnorme.lang.ide.core.operations.DaemonEnginePreferences;
import melnorme.lang.ide.ui.preferences.LangRootPreferencePage;
import melnorme.lang.ide.ui.preferences.LangSDKConfigBlock;

import com.github.rustdt.ide.core.operations.RustBuilder.RustSDKLocationValidator;
import com.github.rustdt.ide.ui.preferences.RustToolsConfigBlock.RustRacerLocationValidator;


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
	protected LangSDKConfigBlock createLangSDKConfigBlock() {
		RustToolsConfigBlock langToolsBlock = new RustToolsConfigBlock();
		
		connectStringField(SDKPreferences.SDK_PATH.key, langToolsBlock.getLocationField(), 
			getSDKValidator());
		
		connectStringField(DaemonEnginePreferences.DAEMON_PATH.key, langToolsBlock.racerGroup.toolLocation, 
			new RustRacerLocationValidator());
		
		return langToolsBlock;
		
	}
	
	@Override
	protected RustSDKLocationValidator getSDKValidator() {
		return new RustSDKLocationValidator();
	}
	
}