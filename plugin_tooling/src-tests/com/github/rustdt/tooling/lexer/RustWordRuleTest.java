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
import melnorme.lang.utils.parse.ICharacterReader;

public class RustWordRuleTest extends CommonLexerRuleTest {
	
	@Override
	protected IPredicateLexingRule createLexingRule() {
		RustWordLexerRule<String> wordLexerRule = 
				new RustWordLexerRule<String>("WS", "KW", "KW_BOOL", "KW_SELF", "word", "macro", "number");
		return new IPredicateLexingRule() {
			@Override
			public boolean doEvaluate(ICharacterReader reader) {
				return wordLexerRule.doEvaluateToken(reader) != null;
			}
		};
	}
	
	@Test
	public void testWordRule() throws Exception { testWordRule$(); }
	public void testWordRule$() throws Exception {
		testRule("", 0);
		testRule("2", 1);
		testRule("2abc", 1);
		testRule("##", 0);
		testRule("a", 1);
		testRule("abc", 3);
		testRule("abc123", 6);
		testRule("abc12x", 6);
		
		testRule("abc##a", 3);
		
		// Test macros
		testRule("abc!(asdf)", 4);
		testRule("abc![a", 4);
		testRule("abc! [a", 4);
		// Test macros (negative cases)
		testRule("abc!=3", 3);
		testRule("abc! asdf", 3);
	}
	
}