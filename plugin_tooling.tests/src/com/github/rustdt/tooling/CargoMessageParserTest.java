/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.CoreUtil.areEqual;

import java.io.StringReader;

import org.junit.Test;

import com.github.rustdt.tooling.CargoMessage.CargoMessageTarget;

import melnorme.utilbox.core.CommonException;

public class CargoMessageParserTest extends CommonRustMessageParserTest {
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		checkEqual(parseMessage(""), null);
		checkEqual(parseMessage("  "), null);
		
		checkEqual(
			parseMessage(getClassResource("cargo_error.json")),
			
			new CargoMessage(
				"compiler-message", 
				"hello_world 0.0.1 (path+file:///D:/devel/Scratchpad.D/RustTest)", 
				new CargoMessageTarget(list("lib"), "hello_world", "src/learn.rs"), 
				RustMessageParserTest.MSG_Simple
			)
		);
		
		checkEqual(
			parseMessage(getClassResource("cargo_message_other.json")),
			
			null
		);
	}
	
	public void checkEqual(CargoMessage obtained, CargoMessage expected) {
		if(!areEqual(expected, obtained)) {
			assertEquals(obtained.reason, expected.reason);
			assertEquals(obtained.packageId, expected.packageId);
			assertEquals(obtained.target, expected.target);
			assertEquals(obtained.message, expected.message);
			
			assertFail();
		}
	}
	
	public CargoMessage parseMessage(String classResource) throws CommonException {
		CargoMessageParser parser = new CargoMessageParser(new StringReader(classResource));
		return parser.parseCargoMessage();
	}
	
}