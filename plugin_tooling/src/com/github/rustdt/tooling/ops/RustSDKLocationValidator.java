/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling.ops;

import java.text.MessageFormat;

import melnorme.lang.tooling.data.SDKLocationValidator;
import melnorme.utilbox.misc.Location;

public class RustSDKLocationValidator extends SDKLocationValidator {
	
	public RustSDKLocationValidator() {
		super("Rust installation:");
	}
	
	@Override
	protected String getSDKExecutable_append() {
		return "bin/cargo"; 
	}
	
	@Override
	protected String getSDKExecutableErrorMessage(Location exeLocation) {
		return MessageFormat.format("Cargo executable not found at Rust location (`{0}`). ", exeLocation);
	}
	
}