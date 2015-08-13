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
import static melnorme.utilbox.misc.StringUtil.nullAsEmpty;

import java.text.MessageFormat;

import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.misc.HashcodeUtil;

/**
 * A Cargo crate manifest 
 */
public class CrateManifest {
	
	/* -----------------  ----------------- */
	
	public static class DependencyRef {
		
		protected final String name;
		protected final String version;
		protected final boolean optional;
		
		public DependencyRef(String name, String version) {
			this(name, version, false);
		}
		
		public DependencyRef(String name, String version, boolean optional) {
			this.name = name;
			this.version = version;
			this.optional = optional;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof DependencyRef)) return false;
			
			DependencyRef other = (DependencyRef) obj;
			
			return 
				areEqual(name, other.name) &&
				areEqual(version, other.version) &&
				areEqual(optional, other.optional)
			;
		}
		
		@Override
		public int hashCode() {
			return HashcodeUtil.combinedHashCode(name, version, optional);
		}
		
		@Override
		public String toString() {
			return MessageFormat.format("{0}@{1}{2}", name, nullAsEmpty(version), optional ? "OPT" : "");
		}
		
	}
	
	/* -----------------  ----------------- */
	
	protected final String name;
	protected final String version;
	protected final Indexable<DependencyRef> deps;
	
	public CrateManifest(String name, String version, Indexable<DependencyRef> deps) {
		this.name = assertNotNull(name);
		this.version = version;
		this.deps = deps;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof CrateManifest)) return false;
		
		CrateManifest other = (CrateManifest) obj;
		
		return 
			areEqual(name, other.name) &&
			areEqual(version, other.version) &&
			areEqual(deps, other.deps)
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
	
}