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

import java.text.MessageFormat;

import melnorme.lang.ide.ui.preferences.LangRootPreferencePage;
import melnorme.utilbox.misc.Location;


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
	protected RustSDKLocationValidator getSDKValidator() {
		return new RustSDKLocationValidator();
	}
	
	protected static class RustSDKLocationValidator extends SDKLocationValidator {
		
		@Override
		protected String getSDKExecutable_append() {
			return "bin/cargo";
		}
		
		@Override
		protected String getSDKExecutableErrorMessage(Location exeLocation) {
			return MessageFormat.format("Cargo executable not found at Rust SDK location (`{0}`). ", exeLocation);
		}
		
	}
	
}