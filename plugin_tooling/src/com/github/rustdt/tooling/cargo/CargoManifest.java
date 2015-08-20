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

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.EnumSet;

import melnorme.lang.tooling.bundle.DependencyRef;
import melnorme.lang.tooling.bundle.FileRef;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Collection2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.collections.LinkedHashMap2;
import melnorme.utilbox.misc.HashcodeUtil;
import melnorme.utilbox.misc.Location;

/**
 * A Cargo crate manifest 
 */
public class CargoManifest {
	
	/* -----------------  ----------------- */
	
	public static class CrateDependencyRef extends DependencyRef {
		
		public CrateDependencyRef(String bundleName, String version) {
			super(bundleName, version);
		}
		
		public CrateDependencyRef(String bundleName, String version, boolean optional) {
			super(bundleName, version, optional);
		}
		
	}
	
	public static LinkedHashMap2<String, CrateDependencyRef> depsHashMap(Indexable<CrateDependencyRef> deps) {
		LinkedHashMap2<String, CrateDependencyRef> depsMap = new LinkedHashMap2<>();
		for(CrateDependencyRef dependencyRef : nullToEmpty(deps)) {
			depsMap.put(dependencyRef.getBundleName(), dependencyRef);
		}
		return depsMap;
	}
	
	/* -----------------  ----------------- */
	
	public static LinkedHashMap2<String, FileRef> binariesHashMap(Indexable<FileRef> deps) {
		LinkedHashMap2<String, FileRef> depsMap = new LinkedHashMap2<>();
		for(FileRef dependencyRef : nullToEmpty(deps)) {
			depsMap.put(dependencyRef.getBinaryPathString(), dependencyRef);
		}
		return depsMap;
	}
	
	/* -----------------  ----------------- */
	
	protected final String name;
	protected final String version;
	protected final LinkedHashMap2<String, CrateDependencyRef> depsMap;
	protected final LinkedHashMap2<String, FileRef> binariesMap;
	
	public CargoManifest(String name, String version, Indexable<CrateDependencyRef> deps, 
			Indexable<FileRef> binaries) {
		this.name = assertNotNull(name);
		this.version = version;
		this.depsMap = depsHashMap(deps);
		this.binariesMap = binariesHashMap(binaries);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof CargoManifest)) return false;
		
		CargoManifest other = (CargoManifest) obj;
		
		return 
			areEqual(name, other.name) &&
			areEqual(version, other.version) &&
			areEqual(depsMap, other.depsMap) &&
			areEqual(binariesMap, other.binariesMap)
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
	
	public Collection2<FileRef> getBinaries() {
		return binariesMap.getValuesView();
	}
	
	/* -----------------  ----------------- */
	
	public Collection2<FileRef> getEffectiveBinaries(Location crateLocation) {
		if(!binariesMap.isEmpty()) {
			return getBinaries();
		}
		ArrayList2<FileRef> binaries = new ArrayList2<>();
		
		Location srcLoc = crateLocation.resolve_fromValid("src");
		if(srcLoc.resolve_valid("main.rs").toFile().exists()) {
			binaries.add(new FileRef(name, null));
		}
		
		Location srcBinLoc = srcLoc.resolve_fromValid("bin");
		
		
		try {
			Files.walkFileTree(srcBinLoc.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1,
				new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
					String fileName = filePath.getFileName().toString();
					
					String crateNameRef = RustNamingRules.getCrateNameRef(fileName);
					
					if(crateNameRef != null) {
						Path sourcePath = crateLocation.relativize(Location.create_fromValid(filePath));
						
						binaries.add(new FileRef(crateNameRef, sourcePath.toString()));
					}
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
				
			});
		} catch(IOException e) {
			// Ignore
		}
		
		return binaries;
	}
	
}