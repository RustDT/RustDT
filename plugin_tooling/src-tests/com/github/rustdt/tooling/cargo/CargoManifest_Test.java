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
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.PathUtil;

public class CargoManifest_Test extends CommonToolingTest {
	
	public static final Location CARGO_BUNDLES = getTestResourceLoc("cargo");
	public static final Location LOC_CRATE_SIMPLE = getTestResourceLoc("cargo/crateSimple");
	public static final Location LOC_CRATE_SIMPLE_LIB = getTestResourceLoc("cargo/crateSimpleLib");
	public static final Location LOC_CRATE_A = getTestResourceLoc("cargo/crateA");
	
	@Test
	public void testEffectiveBinaries() throws Exception { testEffectiveBinaries$(); }
	public void testEffectiveBinaries$() throws Exception {
		CargoManifestParser parser = new CargoManifestParser();
		
		// Test implicit binary
		CargoManifest cargoBin1 = parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("CrateBin1.toml")));
		
		assertEquals(cargoBin1.getEffectiveBinaries(LOC_CRATE_SIMPLE),
			new ArrayList2<>(
				new FileRef("hello_world", null)
			)
		);
		
		assertEquals(cargoBin1.getEffectiveBinaries(LOC_CRATE_SIMPLE_LIB),
			new ArrayList2<>(
			)
		);
		
		assertEquals(cargoBin1.getEffectiveBinaries(LOC_CRATE_A),
			new ArrayList2<>(
				new FileRef("hello_world", null),
				new FileRef("bin1", path("src/bin/bin1.rs").toString()),
				new FileRef("bin2", path("src/bin/bin2.rs").toString())
			)
		);
		
		FileRef cargoBin1__hello_world = cargoBin1.getEffectiveBinaries(LOC_CRATE_A).toArrayList().get(0);
		assertAreEqual(cargoBin1__hello_world.getBinaryPath(), PathUtil.createPath("hello_world"));
		assertAreEqual(cargoBin1__hello_world.getSourcePath(), null);
	}
	
	
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
	
}