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
import melnorme.lang.tooling.parser.lexer.IPredicateLexingRule;

public class RustRawStringLiteralRule_Test extends CommonLexerRuleTest {
	
	@Override
	protected IPredicateLexingRule createLexingRule() {
		return new RustRawStringLiteralRule();
	}
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		testRule("", false);
		testRule("r", 0);
		testRule("r|", false);
		testRule("r||");
		testRule("r|asd|");
		testRule("br|xx\\|");
		
		testRule("r#|xx\\|xx#|xx\\|#");
		testRule("br##|xx|xx#|#xx|##");
		testRule("br##|xx|xx#|#xx|", false);
		testRule("br##|xx#xx|#", false);
	}
	
	@Override
	public void testRule(String source, int expectedTokenLength) {
		source = source.replace('|', '"');
		super.testRule(source, expectedTokenLength);
	}
	
}