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
	
	public static LinkedHashMap2<String, FileRef> fileRefHashMap(Indexable<FileRef> deps) {
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
	protected final LinkedHashMap2<String, FileRef> testsMap;
	
	public CargoManifest(String name, String version, Indexable<CrateDependencyRef> deps, 
			Indexable<FileRef> binaries, Indexable<FileRef> tests) {
		this.name = assertNotNull(name);
		this.version = version;
		this.depsMap = depsHashMap(deps);
		this.binariesMap = fileRefHashMap(binaries);
		this.testsMap = fileRefHashMap(tests);
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
	
	public Collection2<FileRef> getTests() {
		return testsMap.getValuesView();
	}
	
	/* -----------------  ----------------- */
	
	protected Location getSrcLocation(Location crateLocation) {
		return crateLocation.resolve_fromValid("src");
	}
	
	public Collection2<FileRef> getEffectiveBinaries(Location crateLocation) {
		if(!binariesMap.isEmpty()) {
			return getBinaries();
		}
		ArrayList2<FileRef> binaries = new ArrayList2<>();
		
		Location srcLoc = getSrcLocation(crateLocation);
		if(srcLoc.resolve_valid("main.rs").toFile().exists()) {
			binaries.add(new FileRef(name, null));
		}
		
		Location srcBinLoc = srcLoc.resolve_fromValid("bin");
		
		binaries.addAll2(new RustFileCollector().find(crateLocation, srcBinLoc));
		
		return binaries;
	}
	
	public static class Foo {
		
	}
	
	public static class RustFileCollector {
		
		protected final ArrayList2<FileRef> fileRefs = new ArrayList2<>();
		
		protected ArrayList2<FileRef> find(Location rootLocation, Location loc) {
			try {
				find_do(rootLocation, loc);
			} catch(IOException e) {
				// Ignore
			}
			
			return fileRefs;
		}
		
		protected void find_do(Location rootLocation, Location loc) throws IOException {
			Files.walkFileTree(loc.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1,
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
						Path sourcePath = rootLocation.relativize(Location.create_fromValid(filePath));
						
						fileRefs.add(new FileRef(crateNameRef, sourcePath.toString()));
					}
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
				
			});
		}
		
	}
	
	protected Location getTestsLocation(Location cargoLocaction) {
		return cargoLocaction.resolve_fromValid("tests");
	}
	
	public Collection2<FileRef> getEffectiveTestBinaries(Location cargoLocaction) {
		if(!testsMap.isEmpty()) {
			return getTests();
		}
		ArrayList2<FileRef> binaries = new ArrayList2<>();
		
		Location testsLoc = getTestsLocation(cargoLocaction);
		
		binaries.addAll2(new RustFileCollector().find(cargoLocaction, testsLoc));
		return binaries;
	}
	
}