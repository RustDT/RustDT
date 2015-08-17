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

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.CoreUtil.areEqual;
import static melnorme.utilbox.core.CoreUtil.nullToEmpty;
import static melnorme.utilbox.misc.StringUtil.nullAsEmpty;

import java.text.MessageFormat;

import melnorme.lang.tooling.bundle.DependencyRef;
import melnorme.utilbox.collections.Collection2;
import melnorme.utilbox.collections.HashMap2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.misc.HashcodeUtil;

/**
 * A Cargo crate manifest 
 */
public class CrateManifest {
	
	/* -----------------  ----------------- */
	
	public static class CrateDependencyRef extends DependencyRef {
		/* FIXME:  refactor to Lang */
		protected final boolean optional;
		
		public CrateDependencyRef(String name, String version) {
			this(name, version, false);
		}
		
		public CrateDependencyRef(String name, String version, boolean optional) {
			super(name, version);
			this.optional = optional;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof CrateDependencyRef)) return false;
			
			CrateDependencyRef other = (CrateDependencyRef) obj;
			
			return 
				areEqual(bundleName, other.bundleName) &&
				areEqual(version, other.version) &&
				areEqual(optional, other.optional)
			;
		}
		
		@Override
		public int hashCode() {
			return HashcodeUtil.combinedHashCode(bundleName, version, optional);
		}
		
		@Override
		public String toString() {
			return MessageFormat.format("{0}@{1}{2}", bundleName, nullAsEmpty(version), optional ? " OPT" : "");
		}
		
	}
	
	public static HashMap2<String, CrateDependencyRef> toHashMap(Indexable<CrateDependencyRef> deps) {
		HashMap2<String, CrateDependencyRef> depsMap = new HashMap2<>();
		for(CrateDependencyRef dependencyRef : nullToEmpty(deps)) {
			depsMap.put(dependencyRef.getBundleName(), dependencyRef);
		}
		return depsMap;
	}
	
	/* -----------------  ----------------- */
	
	protected final String name;
	protected final String version;
	protected final HashMap2<String, CrateDependencyRef> depsMap;
	
	public CrateManifest(String name, String version, Indexable<CrateDependencyRef> deps) {
		this.name = assertNotNull(name);
		this.version = version;
		this.depsMap = toHashMap(deps);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof CrateManifest)) return false;
		
		CrateManifest other = (CrateManifest) obj;
		
		return 
			areEqual(name, other.name) &&
			areEqual(version, other.version) &&
			areEqual(depsMap, other.depsMap)
		;
	}
	
	@Override
	public int hashCode() {
		return HashcodeUtil.combinedHashCode(name, version);
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("{0}[{1}]", name, nullAsEmpty(version));
	}
	
	/* -----------------  ----------------- */
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public Collection2<CrateDependencyRef> getDependencies() {
		return depsMap.getValuesView();
	}
	
}