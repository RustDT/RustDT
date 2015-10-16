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

public class RustAttributeRulesTest extends CommonLexerRuleTest {
	
	@Override
	protected ILexingRule createLexingRule() {
		return new RustAttributeRule();
	}
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		testRule("", 0);
		testRule("abc", 0);
		
		testRule("#[", false);
		testRule("#[]");
		testRule("#[abc", false);
		testRule("#[abc]");
		
		// Test parse strings
		testRule("#[ blah= \"abc \n' \" --- ]");
		testRule("#[ blah= \"abc \n' \"]");
		testRule("#[ blah= \"abc ]   \" \n ]");
		testRule("#[ blah= \"abc", false);
		
		// test escape char in string
		testRule("#[ \"abc\\\"]def\\", false);
		testRule("#[ \"abc\\\"]def\\]", false);
		testRule("#[ \"abc\\\"]def\"]");
		
		testRule(getClassResource("attribute_sample.rs"));
	}
	
	@Override
	public void testRule(String source, int expectedTokenLength) {
		super.testRule(source, expectedTokenLength);
		if(source.startsWith("#[")) {
			super.testRule(source.replace("#[", "#!["), expectedTokenLength + 1);
		}
	}
	
}