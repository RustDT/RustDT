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

import org.junit.Test;

import com.github.rustdt.tooling.cargo.CargoManifest.CrateDependencyRef;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tests.LangToolingTestResources;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;

public class CargoManifestParser_Test extends CommonToolingTest {
	
	public static final Location CARGO_BUNDLES = LangToolingTestResources.getTestResourceLoc("cargo");
	
	@Test
	public void testParsing() throws Exception { testParsing$(); }
	public void testParsing$() throws Exception {
		CargoManifestParser parser = new CargoManifestParser();
		
		assertEquals(parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("BasicCrate.toml"))), 
			new CargoManifest("hello_world", "0.1.0", new ArrayList2<>()));
		
		
		verifyThrows(() -> parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("BasicCrate.no_name.toml"))),
			CommonException.class, "Value for key `name` is missing");
		verifyThrows(() -> parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("BasicCrate.invalid_name.toml"))),
			CommonException.class, "Value for key `name` is not a String");
		
		assertEquals(parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("BasicCrate.empty_name.toml"))), 
			new CargoManifest("", "0.1.0", new ArrayList2<>()));
		
		
		verifyThrows(() -> parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("Crate1.no_package.toml"))),
			CommonException.class, "Value for key `package` is missing");
		verifyThrows(() -> parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("Crate1.invalid_package.toml"))),
			CommonException.class, "Value for key `package` is not a Map");
		
		
		CargoManifest CRATE_DEPS = new CargoManifest("hello_world", null,
			new ArrayList2<>(
				new CrateDependencyRef("rand", "0.3.0"),
				new CrateDependencyRef("dep_empty", ""),
				new CrateDependencyRef("dep_invalid", null),
				new CrateDependencyRef("dep_git", null, false),
				new CrateDependencyRef("dep_foo", "1.2.0", true)
			)
		);
		
		assertEquals(parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("CrateDeps.toml"))),
			CRATE_DEPS
		);
		
		// Same contents, different toml format
		assertEquals(parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("CrateDeps2.toml"))),
			CRATE_DEPS
		);
		
	}
	
}