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

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tests.LangToolingTestResources;
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
		CargoManifestParser manifestParser = new CargoManifestParser();
		
		assertEquals(manifestParser.parse(readStringFromFile(CARGO_BUNDLES.resolve("BasicCrate.toml"))), 
			new CrateManifest("hello_world", "0.1.0"));
	}
	
}