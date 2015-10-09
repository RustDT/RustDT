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
import com.github.rustdt.tooling.ops.RacerOperation.RustRacerLocationValidator;
import com.github.rustdt.tooling.ops.RustSDKLocationValidator;
import com.github.rustdt.tooling.ops.RustSDKSrcLocationValidator;

import melnorme.lang.ide.core.operations.ToolchainPreferences;
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
	protected RustToolsConfigBlock doCreateLangSDKConfigBlock() {
		return new RustToolsConfigBlock();
	}
	
	@Override
	protected LangSDKConfigBlock createLangSDKConfigBlock() {
		RustToolsConfigBlock langToolsBlock = doCreateLangSDKConfigBlock();
		
		connectStringField(ToolchainPreferences.SDK_PATH.key, langToolsBlock.getLocationField(), 
			getSDKValidator());
		
		connectStringField(RustSDKPreferences.SDK_SRC_PATH2.getKey(), langToolsBlock.sdkSrcLocation, 
			new RustSDKSrcLocationValidator());
		
		connectStringField(RustSDKPreferences.RACER_PATH.getKey(), langToolsBlock.racerGroup.racerLocation, 
			new RustRacerLocationValidator());
		
		return langToolsBlock;
	}
	
	@Override
	protected RustSDKLocationValidator getSDKValidator() {
		return new RustSDKLocationValidator();
	}
	
}