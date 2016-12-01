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

import static com.github.rustdt.tooling.lexer.RustWordLexer_Test.ColoringTokens.KEYWORD;
import static com.github.rustdt.tooling.lexer.RustWordLexer_Test.ColoringTokens.KEYWORD_BOOL;
import static com.github.rustdt.tooling.lexer.RustWordLexer_Test.ColoringTokens.KEYWORD_SELF;
import static com.github.rustdt.tooling.lexer.RustWordLexer_Test.ColoringTokens.MACRO_CALL;
import static com.github.rustdt.tooling.lexer.RustWordLexer_Test.ColoringTokens.NUMBER;
import static com.github.rustdt.tooling.lexer.RustWordLexer_Test.ColoringTokens.TRY_OP;
import static com.github.rustdt.tooling.lexer.RustWordLexer_Test.ColoringTokens.WORD;
import static com.github.rustdt.tooling.lexer.RustWordLexer_Test.ColoringTokens.WS;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import org.junit.Test;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.utils.parse.StringCharSource;

public class RustWordLexer_Test extends CommonToolingTest {
	
	public enum ColoringTokens {
		WS,
		KEYWORD,
		KEYWORD_BOOL,
		KEYWORD_SELF,
		WORD,
		MACRO_CALL,
		NUMBER,
		TRY_OP,
	}
	
	public void testRule(String source, ColoringTokens expectedToken, int expectedLength) {
		RustWordLexerRule<ColoringTokens> wordLexerRule = new RustWordLexerRule<>(
			WS,
			KEYWORD,
			KEYWORD_BOOL,
			KEYWORD_SELF,
			WORD,
			MACRO_CALL,
			NUMBER,
			TRY_OP
		);
		
		StringCharSource reader = new StringCharSource(source);
		ColoringTokens token = wordLexerRule.doEvaluateToken(reader);
		
		assertTrue(token == expectedToken);
		assertTrue(reader.getReadPosition() == expectedLength);
	}
	
	@Test
	public void testWordRule() throws Exception { testWordRule$(); }
	public void testWordRule$() throws Exception {
		testRule("", null, 0);
		testRule("2", NUMBER, 1);
		testRule("2abc", NUMBER, 1);
		testRule("##", null, 0);
		testRule("a", WORD, 1);
		testRule("abc", WORD, 3);
		testRule("abc123", WORD, 6);
		testRule("abc12x", WORD, 6);
		
		testRule("abc##a", WORD, 3);
		
		testRule("?", TRY_OP, 1);
		testRule("?asdf", TRY_OP, 1);
		
		// Test macros
		testRule("abc!(asdf)", MACRO_CALL, 4);
		testRule("abc![a", MACRO_CALL, 4);
		testRule("abc! [a", MACRO_CALL, 4);
		// Test macros (negative cases)
		testRule("abc!=3", WORD, 3);
		testRule("abc! asdf", WORD, 3);
	}
	
}