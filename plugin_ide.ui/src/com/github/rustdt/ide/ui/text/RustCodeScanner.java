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

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.github.rustdt.tooling.lexer.RustWordLexerRule;

import melnorme.lang.ide.ui.text.AbstractLangScanner;
import melnorme.lang.ide.ui.text.coloring.StylingPreferences;
import melnorme.lang.ide.ui.text.coloring.TokenRegistry;
import melnorme.utilbox.collections.ArrayList2;

/**
 * Rust scanner for the code partition
 */
public class RustCodeScanner extends AbstractLangScanner {
	
	public RustCodeScanner(TokenRegistry tokenStore, StylingPreferences stylingPrefs) {
		super(tokenStore, stylingPrefs);
	}
	
	@Override
	protected void initRules(ArrayList2<IRule> rules) {
		IToken defaultToken = getToken(RustColorPreferences.DEFAULT);
		setDefaultReturnToken(defaultToken);
		
		RustWordLexerRule<IToken> codeLexerRule = new RustWordLexerRule<>(
			Token.WHITESPACE, 
			getToken(RustColorPreferences.KEYWORDS),
			getToken(RustColorPreferences.KEYWORDS_VALUES),
			defaultToken,
			getToken(RustColorPreferences.MACRO_CALL),
			getToken(RustColorPreferences.NUMBERS)
		);
		
		rules.add(new LexingRule_RuleAdapter(codeLexerRule));
	}
	
}