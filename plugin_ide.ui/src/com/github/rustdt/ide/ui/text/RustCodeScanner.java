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
package com.github.rustdt.ide.ui.text;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.github.rustdt.tooling.lexer.RustColoringTokens;
import com.github.rustdt.tooling.lexer.RustWordLexerRule;

import melnorme.lang.ide.ui.text.AbstractLangScanner;
import melnorme.lang.ide.ui.text.coloring.TokenRegistry;
import melnorme.lang.tooling.parser.lexer.ILexingRule2;
import melnorme.lang.utils.parse.ICharacterReader;
import melnorme.utilbox.collections.ArrayList2;

/**
 * Rust scanner for the code partition
 */
public class RustCodeScanner extends AbstractLangScanner {
	
	public RustCodeScanner(TokenRegistry tokenStore) {
		super(tokenStore);
	}
	
	@Override
	protected void initRules(ArrayList2<IRule> rules) {
		IToken defaultToken = getToken(RustColorPreferences.DEFAULT);
		setDefaultReturnToken(defaultToken);
		
		IToken tkWhitespace = Token.WHITESPACE;
		IToken tkKeywords = getToken(RustColorPreferences.KEYWORDS);
		IToken tkKeywordsBooleanLiteral = getToken(RustColorPreferences.KEYWORDS_BOOLEAN_LIT);
		IToken tkKeywordsSelf = getToken(RustColorPreferences.KEYWORDS_SELF);
		IToken tkMacroCall = getToken(RustColorPreferences.MACRO_CALL);
		IToken tkNumbers = getToken(RustColorPreferences.NUMBERS);
		IToken tkTryOperator = getToken(RustColorPreferences.TRY_OPERATOR);
		
		RustWordLexerRule rustColorLexer = new RustWordLexerRule();
		
		ILexingRule2<IToken> rule = new ILexingRule2<IToken>() {
			@Override
			public IToken doEvaluateToken(ICharacterReader reader) {
				RustColoringTokens rustToken = rustColorLexer.evaluateToken(reader);
				if(rustToken == null) {
					return null;
				}
				switch (rustToken) {
				case WS: return tkWhitespace;
				case KEYWORD: return tkKeywords;
				case KEYWORD_BOOL: return tkKeywordsBooleanLiteral;
				case KEYWORD_SELF: return tkKeywordsSelf;
				case WORD: return defaultToken;
				case MACRO_CALL: return tkMacroCall;
				case MACRO_RULES: return tkMacroCall; // TODO own coloring token
				case NUMBER: return tkNumbers;
				case TRY_OP: return tkTryOperator;
				}
				// switch must be complete
				throw assertFail();
			}
		};
		
		rules.add(new LexingRule_RuleAdapter(rule));
	}
	
}