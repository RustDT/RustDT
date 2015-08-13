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

import me.grison.jtoml.impl.Toml;
import melnorme.utilbox.core.CommonException;

public class TomlHelper {
	
	protected final Toml toml;
	
	public TomlHelper(Toml toml) {
		this.toml = toml;
	}
	
	public Map<String, Object> getMap(String key) throws CommonException {
		try {
			return toml.getMap(key);
		} catch(IllegalArgumentException e) {
			throw new CommonException(e.getMessage());
		}
	}
	
	public String getString(String key) throws CommonException {
		try {
			return toml.getString(key);
		} catch(IllegalArgumentException e) {
			throw new CommonException(e.getMessage());
		}
	}
	
	public List<Object> getList(String key) throws CommonException {
		try {
			return toml.getList(key);
		} catch(IllegalArgumentException e) {
			throw new CommonException(e.getMessage());
		}
	}
	
}