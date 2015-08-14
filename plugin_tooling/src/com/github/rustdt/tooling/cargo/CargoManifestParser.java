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
import java.util.Map.Entry;

import com.github.rustdt.tooling.cargo.CrateManifest.DependencyRef;
import com.moandjiezana.toml.Toml;

import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.core.CoreUtil;
import melnorme.utilbox.misc.PathUtil;

public class CargoManifestParser {
	
	public static enum FAllowNull { YES, NO ; public boolean isTrue() { return this == YES; } }

	public static final Path BUNDLE_MANIFEST_FILE = PathUtil.createValidPath("Cargo.toml");
	
	protected MapHelper helper = new MapHelper();
	
	public CrateManifest parse(String source) throws CommonException {
		
		Toml toml = new Toml().read(source);
		Map<String, Object> manifestMap = toml.getValues();
		
		Map<String, Object> packageEntry = helper.getTable(manifestMap, "package", FAllowNull.NO);
		
		String name = helper.getString(packageEntry, "name", FAllowNull.NO);
		String version = helper.getString(packageEntry, "version", FAllowNull.YES);
		
		ArrayList2<DependencyRef> deps = parseDeps(manifestMap);
		
		return new CrateManifest(
			name, 
			version, 
			deps);
	}
	
	protected ArrayList2<DependencyRef> parseDeps(Map<String, Object> manifestMap) throws CommonException {
		
		ArrayList2<DependencyRef> deps = new ArrayList2<>();
		
		Map<String, Object> depsMap = helper.getTable(manifestMap, "dependencies", FAllowNull.YES);
		if(depsMap != null) {
			for(Entry<String, Object> depsEntry : depsMap.entrySet()) {
				String name = depsEntry.getKey();
				Object value = depsEntry.getValue();
				
				if(value instanceof Map<?, ?>) {
					Map<String, Object> map = CoreUtil.blindCast(value);
					deps.add(parseDependencyRef(helper, name, map));
					continue;
				}
				
				String version = null;
				if(value instanceof String) {
					version = (String) value;
				}
				deps.add(new DependencyRef(name, version));
			}
		}
		
		return deps;
	}
	
	protected DependencyRef parseDependencyRef(MapHelper helper, String name, Map<String, Object> map) {
		String version = helper.getValue_ignoreErrors(map, "version", String.class, null);
		boolean optional = helper.getValue_ignoreErrors(map, "optional", Boolean.class, false);
		
		return new DependencyRef(name, version, optional);
	}
	
}