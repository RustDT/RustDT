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
import melnorme.lang.tooling.bundle.FileRef;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.PathUtil;

public class CargoManifest_Test extends CommonToolingTest {
	
	public static final Location CARGO_BUNDLES = LangToolingTestResources.getTestResourceLoc("cargo");
	
	@Test
	public void testSemantics() throws Exception { testSemantics$(); }
	public void testSemantics$() throws Exception {
		CargoManifestParser parser = new CargoManifestParser();
		
		// Test implicit binary
		CargoManifest cargoBin1 = parser.parse(readStringFromFile(CARGO_BUNDLES.resolve("CrateBin1.toml")));
		
		assertEquals(cargoBin1.getEffectiveBinaries(),
			new ArrayList2<>(
				new FileRef("hello_world", null)
			)
		);
		
		FileRef cargoBin1__hello_world = cargoBin1.getEffectiveBinaries().toArrayList().get(0);
		assertAreEqual(cargoBin1__hello_world.getBinaryPath(), PathUtil.createPath("hello_world"));
		assertAreEqual(cargoBin1__hello_world.getSourcePath(), null);
	}
	
}