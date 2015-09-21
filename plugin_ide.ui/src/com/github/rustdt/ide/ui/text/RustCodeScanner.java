/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
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

import _org.eclipse.cdt.ui.text.ITokenStoreFactory;
import melnorme.lang.ide.ui.text.AbstractLangScanner;
import melnorme.utilbox.collections.ArrayList2;

/**
 * Rust scanner for the code partition
 */
public class RustCodeScanner extends AbstractLangScanner {
	
	private static String tokenPrefProperties[] = new String[] {
		RustColorPreferences.DEFAULT.key,
		RustColorPreferences.KEYWORDS.key,
		RustColorPreferences.KEYWORDS_VALUES.key,
		RustColorPreferences.MACRO_CALL.key,
	};
	
	
	public RustCodeScanner(ITokenStoreFactory factory) {
		super(factory.createTokenStore(tokenPrefProperties));
	}
	
	@Override
	protected void initRules(ArrayList2<IRule> rules) {
		IToken defaultToken = getToken(RustColorPreferences.DEFAULT.key);
		setDefaultReturnToken(defaultToken);
		
		RustWordLexerRule<IToken> codeLexerRule = new RustWordLexerRule<>(
			Token.WHITESPACE, 
			getToken(RustColorPreferences.KEYWORDS.key),
			getToken(RustColorPreferences.KEYWORDS_VALUES.key),
			defaultToken,
			getToken(RustColorPreferences.MACRO_CALL.key)
		);
		
		rules.add(new LexingRule_RuleAdapter(codeLexerRule));
	}
	
}