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

import static melnorme.utilbox.core.CoreUtil.areEqual;

import melnorme.utilbox.misc.HashcodeUtil;

/**
 * A Cargo crate manifest 
 */
public class CrateManifest {
	
	public static class CrateManifestData {
		
		public String name;
		public String version;
		
		public CrateManifestData(String name, String version) {
			this.name = name;
			this.version = version;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof CrateManifest.CrateManifestData)) return false;
			
			CrateManifest.CrateManifestData other = (CrateManifest.CrateManifestData) obj;
			
			return 
				areEqual(name, other.name) &&
				areEqual(version, other.version);
		}
		
		@Override
		public int hashCode() {
			return HashcodeUtil.combinedHashCode(name, version);
		}
		
	}
	
	/* -----------------  ----------------- */
	
	protected final CrateManifestData data;
	
	public CrateManifest(String name, String version) {
		this(new CrateManifestData(name, version)); 
	}
	
	/**
	 * Note: receiver becomes owner of given CrateManifestData instance, no other references to it should be held.
	 */
	public CrateManifest(CrateManifestData data) {
		this.data = data;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof CrateManifest)) return false;
		
		CrateManifest other = (CrateManifest) obj;
		
		return areEqual(data, other.data);
	}
	
	@Override
	public int hashCode() {
		return HashcodeUtil.combinedHashCode(data);
	}
	
}