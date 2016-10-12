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

import static melnorme.lang.tests.LangToolingTestResources.getTestResourceLoc;

import org.junit.Test;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tooling.bundle.FileRef;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.PathUtil;

public class CargoManifest_Test extends CommonToolingTest {
	
	@Test
	public void testName() throws Exception { testName$(); }
	public void testName$() throws Exception {
		assertAreEqual(RustNamingRules.getCrateNameRef(""), null);
		assertAreEqual(RustNamingRules.getCrateNameRef("blah"), null);
		assertAreEqual(RustNamingRules.getCrateNameRef("blah.rs"), "blah");
		assertAreEqual(RustNamingRules.getCrateNameRef("foo-bar_x.rs"), "foo-bar_x");
		assertAreEqual(RustNamingRules.getCrateNameRef("123.rs"), "123");
		
		assertAreEqual(RustNamingRules.getCrateNameRef(".rs"), null);
		assertAreEqual(RustNamingRules.getCrateNameRef("foo.bar.rs"), null);
		assertAreEqual(RustNamingRules.getCrateNameRef("foo,bar.rs"), null);
	}
	
	public static final Location CARGO_BUNDLES = getTestResourceLoc("cargo");
	public static final Location LOC_CRATE_SIMPLE = getTestResourceLoc("cargo/crateSimple");
	public static final Location LOC_CRATE_SIMPLE_LIB = getTestResourceLoc("cargo/crateSimpleLib");
	public static final Location LOC_CRATE_FOO = getTestResourceLoc("cargo/crateFoo");
	
	public static CargoManifest parseManifest(Location location) throws CommonException {
		CargoManifestParser parser = new CargoManifestParser();
		return parser.parse(readStringFromFile(location.resolve("Cargo.toml")));
	}
	
	@Test
	public void testEffectiveBinaries() throws Exception { testEffectiveBinaries$(); }
	public void testEffectiveBinaries$() throws Exception {
		
		testEffectiveTargets(parseManifest(LOC_CRATE_SIMPLE_LIB), LOC_CRATE_SIMPLE_LIB, 
			new FileRef("crate_simple_lib", null), 
			new ArrayList2<>(),
			new ArrayList2<>()
		);
		
		testEffectiveTargets(parseManifest(LOC_CRATE_SIMPLE), LOC_CRATE_SIMPLE,
			null,
			new ArrayList2<>(
				new FileRef("crate_simple", null)
			),
			new ArrayList2<>()
		);
		
		CargoManifest crateFooManifest = parseManifest(LOC_CRATE_FOO);
		testEffectiveTargets(crateFooManifest, LOC_CRATE_FOO,
			new FileRef("crate_foo", null), 
			new ArrayList2<>(
				new FileRef("crate_foo", null),
				new FileRef("bin1", path("src/bin/bin1.rs").toString()),
				new FileRef("bin2", path("src/bin/bin2.rs").toString())
			),
			new ArrayList2<>(
				new FileRef("test1", path("tests/test1.rs").toString()),
				new FileRef("test2", path("tests/test2.rs").toString())
			)
		);
		
		FileRef cargoBin1__hello_world = crateFooManifest.getEffectiveBinaries(LOC_CRATE_FOO).toArrayList().get(0);
		assertAreEqual(cargoBin1__hello_world.getBinaryPath(), PathUtil.createPath("crate_foo"));
		assertAreEqual(cargoBin1__hello_world.getSourcePath(), null);
		
		testGetEffectiveTestTargets______________();
	}
	
	protected void testEffectiveTargets(CargoManifest cargoMf, Location crateLoc, FileRef expectedLibrary, 
			ArrayList2<FileRef> expectedBinaries, ArrayList2<FileRef> expectedIntegrationTests) {
		
		assertAreEqual(cargoMf.getEffectiveBinaries(crateLoc),
			expectedBinaries
		);
		assertAreEqual(cargoMf.getEffectiveLibrary(crateLoc),
			expectedLibrary
		);
		assertAreEqual(cargoMf.getEffectiveIntegrationTests(crateLoc),
			expectedIntegrationTests
		);
	}
	
	protected void testGetEffectiveTestTargets______________() throws CommonException {
		CargoManifest crateFoo = parseManifest(LOC_CRATE_FOO);
		
		assertEquals(crateFoo.getEffectiveTestTargets(LOC_CRATE_FOO),
			new ArrayList2<>(
				"test1",
				"test2",
				"lib.crate_foo",
				"bin.crate_foo",
				"bin.bin1",
				"bin.bin2"
			)
		);
	}
	
}