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
	public void testNumberLexingRuleValid() throws Exception {
		testNumberLexingRuleValid$();
	}

	public void testNumberLexingRuleValid$() throws Exception {
		testNumberRule("-1.01", 5);
		testNumberRule("-1", 2);

		testNumberRule("0", 1);
		testNumberRule("123", 3);
		testNumberRule("123", 3);
		testNumberRule("1u8", 3);
		testNumberRule("2u16", 4);
		testNumberRule("3u32", 4);
		testNumberRule("4u64", 4);
		testNumberRule("1i8", 3);
		testNumberRule("2i16", 4);
		testNumberRule("3i32", 4);
		testNumberRule("4i64", 4);

		testNumberRule("123.0", 5);
		testNumberRule("123.1f32", 8);
		testNumberRule("123.2f64", 8);

		testNumberRule("0xFF", 4);
		testNumberRule("0b11", 4);
		testNumberRule("0o77", 4);

		testNumberRule("0xFFu16", 7);
		testNumberRule("0b11u16", 7);
		testNumberRule("0o77u16", 7);
		testNumberRule("0xFFu32", 7);
		testNumberRule("0b11u32", 7);
		testNumberRule("0o77u32", 7);
		testNumberRule("0xFFu64", 7);
		testNumberRule("0b11u64", 7);
		testNumberRule("0o77u64", 7);
		testNumberRule("123..", 3);
		testNumberRule("123,", 3);
		testNumberRule("10_20_30", 8);
	}

	@Test
	public void testNumberLexingRuleInvalid() throws Exception {
		testNumberLexingRuleInvalid$();
	}

	public void testNumberLexingRuleInvalid$() throws Exception {
		testNumberRule("\"5000\"", 0);
		testNumberRule("xxx", 0);
		testNumberRule("", 0);
		testNumberRule("123xxx", 0);
		testNumberRule("123.4xxx", 0);
		testNumberRule("xxx123", 0);
		testNumberRule("0x0.2", 0);
		testNumberRule("0o0.2", 0);
		testNumberRule("0b0.2", 0);
		testNumberRule("0.2u32", 0);
		testNumberRule("0x0xFF", 0);
	}
}