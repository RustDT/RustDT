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
package com.github.rustdt.tooling.cargo;

import java.util.Map;

import com.github.rustdt.tooling.cargo.CargoManifestParser.FAllowNull;

import melnorme.utilbox.core.CommonException;

public class CommonDataValidator {
	
	public CommonDataValidator() {
		super();
	}
	
	public <T> T validate(Object value, Class<T> klass, FAllowNull allowNull, String key) throws CommonException {
		if(value == null && !allowNull.isTrue()) {
			handleNullValue(key);
		}
		
		if(value != null && !klass.isInstance(value)) {
			handleInvalidValue(key);
		}
		return klass.cast(value);
	}
	
	public void handleNullValue(String key) throws CommonException {
		throw CommonException.fromMsgFormat("Value for key `{0}` is missing.", key);
	}
	
	public void handleInvalidValue(String key) throws CommonException {
		throw CommonException.fromMsgFormat("Value for key `{0}` is not a `{1}`.", key);
	}
	
	/* -----------------  ----------------- */
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> validateMap(Object object, String key, FAllowNull allowNull) throws CommonException {
		return validate(object, Map.class, allowNull, key);
	}
	
}