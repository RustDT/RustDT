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

import com.github.rustdt.tooling.cargo.CrateManifest.DependencyRef;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tests.LangToolingTestResources;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;

public class CargoManifestParser_Test extends CommonToolingTest {
	
	public static final Location CARGO_BUNDLES = LangToolingTestResources.getTestResourceLoc("cargo");

	public static class Player {
	    String name;
	    Long score;
	}
	
	@Test
	public void testParsing() throws Exception { testParsing$(); }
	public void testParsing$() throws Exception {
		CargoManifestParser parser = new CargoManifestParser();
		
		assertEquals(parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("BasicCrate.toml"))), 
			new CrateManifest("hello_world", "0.1.0", new ArrayList2<>()));

		
		verifyThrows(() -> parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("BasicCrate.no_name.toml"))),
			CommonException.class, "Value for key `name` is missing");

		assertEquals(parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("BasicCrate.empty_name.toml"))), 
			new CrateManifest("", "0.1.0", new ArrayList2<>()));

		CrateManifest CRATE_DEPS = new CrateManifest("hello_world", null,
			new ArrayList2<>(
				new DependencyRef("rand", "0.3.0"),
				new DependencyRef("dep_empty", ""),
				new DependencyRef("dep_invalid", null),
				new DependencyRef("dep_git", null, false),
				new DependencyRef("dep_foo", "1.2.0", true)
			));
		
		assertEquals(parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("CrateDeps.toml"))),
			CRATE_DEPS
		);
		
		if(false) // Disabled because current jtoml doesn't support this version
		// Same contents, different toml format
		assertEquals(parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("CrateDeps2.toml"))),
			CRATE_DEPS
		);
		
	}
	
}