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

import java.nio.file.Path;
import java.util.Map;

import me.grison.jtoml.impl.Toml;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.PathUtil;

public class CargoManifestParser {
	
	public static enum FAllowNull { YES, NO ; public boolean isTrue() { return this == YES; } }

	public static final Path BUNDLE_MANIFEST_FILE = PathUtil.createValidPath("Cargo.toml");
	
	public CrateManifest parse(String source) throws CommonException {
		
		Toml toml = Toml.parse(source);
		
		TomlHelper helper = new TomlHelper(toml);
		
		Map<String, Object> packageMap = helper.getMap("package");
		String name = getKey(packageMap, "name", String.class, FAllowNull.NO);
		String version = getKey(packageMap, "version", String.class, FAllowNull.YES);
		
		return new CrateManifest(
			name, 
			version);
	}
	
	public <T> T getKey(Map<String, Object> map, String key, Class<T> klass, FAllowNull allowNull) 
			throws CommonException {
		Object value = map.get(key);
		
		if(value == null && allowNull.isTrue()) {
			throw CommonException.fromMsgFormat("Value for key `{0}` is null.", key);
		}
		
		if(value != null && !klass.isInstance(value)) {
			throw CommonException.fromMsgFormat("Value for key `{0}` is not a `{1}`.", key);
		}
		return klass.cast(value);
	}
	
}