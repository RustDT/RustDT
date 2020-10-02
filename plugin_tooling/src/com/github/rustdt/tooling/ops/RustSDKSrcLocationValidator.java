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
package com.github.rustdt.tooling.ops;

import java.text.MessageFormat;

import melnorme.lang.utils.validators.LocationValidator;
import melnorme.utilbox.fields.validation.ValidationException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.status.Severity;

public class RustSDKSrcLocationValidator extends LocationValidator {
	
	public RustSDKSrcLocationValidator() {
		super("Rust 'src' directory:");
		directoryOnly = true;
	}
	
	@Override
	protected Location getValidatedField_rest(Location location) throws ValidationException {
		
		if(!location.resolve_fromValid("libcore").toFile().exists() && !location.resolve_fromValid("core").toFile().exists()) {
			throw createException(Severity.WARNING, 
					MessageFormat.format("Path `{0}` does not contain core library directory.", location));
		}
		
		return location;
	}
	
}