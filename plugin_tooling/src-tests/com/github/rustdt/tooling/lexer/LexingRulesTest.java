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

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tooling.parser.lexer.ILexingRule;
import melnorme.lang.tooling.parser.lexer.StringCharacterReader;

public class LexingRulesTest extends CommonToolingTest {

	public static void testRule(ILexingRule lexRule, String source, int expectedTokenLength) {
		StringCharacterReader reader = new StringCharacterReader(source);
		boolean isMatch = lexRule.evaluate(reader);

		assertEquals(isMatch, expectedTokenLength > 0);
		if (isMatch) {
			assertEquals(reader.getOffset(), expectedTokenLength);
		}
	}

	public static void testNumberRule(String source, int expectedTokenLength) {
		testRule(new RustNumberLexingRule(), source, expectedTokenLength);
	}

	@Test
	public void testNumberLexingRuleValid() throws Exception { testNumberLexingRuleValid$(); }
	public void testNumberLexingRuleValid$() throws Exception {
		
		testNumberRule("", 0);
		testNumberRule("xxx", 0);
		testNumberRule("xxx123", 0);
		
		testNumberRule("0", 1);
		testNumberRule("2", 1);
		testNumberRule("123", 3);
		testNumberRule("12_3_", 5);
		testNumberRule("_", 0);
		
		// parse even the illegal suffixes
		testNumberRule("123xxx", 3);
		testNumberRule("123.4xxx", 5);
		
		
		testNumberRule("0b_010", 6);
		testNumberRule("0o7_17", 6);
		testNumberRule("0xF1F_", 6);
		
		testNumberRule("0b012", 4);
		testNumberRule("0o718", 4);
		testNumberRule("0xF1G", 4);
		
		testNumberRule("1u8", 3);
		testNumberRule("1i8", 3);
		testNumberRule("2u16", 4);
		testNumberRule("2i16", 4);
		testNumberRule("0b1u32", 6);
		testNumberRule("0o7i32", 6);
		testNumberRule("0xAu64", 6);
		testNumberRule("0xAi64", 6);
		
		// Floats
		testNumberRule("123.", 4);
		testNumberRule("123.0", 5);
		testNumberRule("123.1f32", 8);
		testNumberRule("123.2f64", 8);
		
		

		testNumberRule("123..", 3);
		testNumberRule("123,", 3);
		testNumberRule("10_20_30", 8);
		
		testNumberRule("0x0.2", 3);
		testNumberRule("0o0.2", 3);
		testNumberRule("0b0.2", 3);
		testNumberRule("0.2u32", 3);
		testNumberRule("0x0xFF", 3);
		
		testNumberRule("-1", 2);
		testNumberRule("-1.01", 5);

	}
	
}