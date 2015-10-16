/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *     Konstantin Salikhov - test cases for lexing rule
 *******************************************************************************/
package com.github.rustdt.tooling.lexer;

import org.junit.Test;

import melnorme.lang.tests.CommonLexerRuleTest;
import melnorme.lang.tooling.parser.lexer.ILexingRule;

public class RustNumberRuleTest extends CommonLexerRuleTest {
	
	@Override
	protected ILexingRule createLexingRule() {
		return new RustNumberLexingRule();
	}

	@Test
	public void testNumberLexingRuleValid() throws Exception { testNumberLexingRuleValid$(); }
	public void testNumberLexingRuleValid$() throws Exception {
		
		testRule("", 0);
		testRule("xxx", 0);
		testRule("xxx123", 0);
		
		testRule("0", 1);
		testRule("2", 1);
		testRule("123", 3);
		testRule("12_3_", 5);
		testRule("_", 0);
		
		// parse even the illegal suffixes
		testRule("123xxx", 3);
		testRule("123.4xxx", 5);
		
		
		testRule("0b_010", 6);
		testRule("0o7_17", 6);
		testRule("0xF1F_", 6);
		
		testRule("0b012", 4);
		testRule("0o718", 4);
		testRule("0xF1G", 4);
		
		testRule("1u8", 3);
		testRule("1i8", 3);
		testRule("2u16", 4);
		testRule("2i16", 4);
		testRule("0b1u32", 6);
		testRule("0o7i32", 6);
		testRule("0xAu64", 6);
		testRule("0xAi64", 6);
		
		// Floats
		testRule("123.", 4);
		testRule("123.0", 5);
		testRule("123.1f32", 8);
		testRule("123.2f64", 8);
		
		

		testRule("123..", 3);
		testRule("123,", 3);
		testRule("10_20_30", 8);
		
		testRule("0x0.2", 3);
		testRule("0o0.2", 3);
		testRule("0b0.2", 3);
		testRule("0.2u32", 3);
		testRule("0x0xFF", 3);
		
		testRule("-1", 2);
		testRule("-1.01", 5);
	}
	
}