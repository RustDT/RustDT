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

public class RustLifetimeRuleTest extends CommonLexerRuleTest {
	
	@Override
	protected IPredicateLexingRule createLexingRule() {
		return new RustLifetimeLexingRule();
	}
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		testRule("", 0);
		testRule("'", 0);
		testRule("'a");
		testRule("'blah");
	}
	
	@Override
	protected String getRuleNeutralSuffix() {
		return "  xxxx";
	}
	
}