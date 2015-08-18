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

import java.util.List;
import java.util.Map;

import com.github.rustdt.tooling.cargo.CargoManifestParser.FAllowNull;

import melnorme.utilbox.core.CommonException;

/**
 * A helper structure to deserialize data from a map.
 */
public class MapHelper extends CommonDataValidator {
	
	public MapHelper() {
	}
	
	public <T> T getValue(Map<String, Object> map, String key, Class<T> klass, FAllowNull allowNull) 
			throws CommonException {
		Object value = map.get(key);
		return validate(value, klass, allowNull, key);
	}
	
	public <T> T getValue(Map<String, Object> map, String key, Class<T> klass, T defaultValue) 
			throws CommonException {
		Object rawValue = map.get(key);
		
		T value = validate(rawValue, klass, FAllowNull.YES, key);
		return value == null ? defaultValue : value;
	}
	
	public <T> T getValue_ignoreErrors(Map<String, Object> map, String key, Class<T> klass, T defaultValue) {
		try {
			return getValue(map, key, klass, defaultValue);
		} catch(CommonException e) {
			return defaultValue;
		}
	}
	
	/* -----------------  ----------------- */
	
	public String getString(Map<String, Object> map, String key, FAllowNull allowNull) 
			throws CommonException {
		return getValue(map, key, String.class, allowNull);
	}
	
	public Boolean getBoolean(Map<String, Object> map, String key, FAllowNull allowNull) 
			throws CommonException {
		return getValue(map, key, Boolean.class, allowNull);
	}
	
	public Float getFloat(Map<String, Object> map, String key, FAllowNull allowNull) 
			throws CommonException {
		return getValue(map, key, Float.class, allowNull);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getTable(Map<String, Object> map, String key, FAllowNull allowNull) 
			throws CommonException {
		return getValue(map, key, Map.class, allowNull);
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> getList(Map<String, Object> map, String key, FAllowNull allowNull) 
			throws CommonException {
		return getValue(map, key, List.class, allowNull);
	}
	
}