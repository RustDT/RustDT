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

import java.util.ArrayList;
import java.util.List;

import melnorme.lang.ide.ui.text.coloring.AbstractLangScanner;

import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * Sample Rust code scanner
 */
public class RustCodeScanner extends AbstractLangScanner {
	
	private static String tokenPrefProperties[] = new String[] {
		RustColorPreferences.DEFAULT.key,
		RustColorPreferences.KEYWORDS.key,
		RustColorPreferences.KEYWORDS_VALUES.key,
	};
	
	public RustCodeScanner(ITokenStoreFactory factory) {
		super(factory.createTokenStore(tokenPrefProperties));
		setRules(createRules());
	}
	
	protected List<IRule> createRules() {
		List<IRule> rules = new ArrayList<IRule>();
		
		IToken tkOther = getToken(RustColorPreferences.DEFAULT.key);
		IToken tkKeywords = getToken(RustColorPreferences.KEYWORDS.key);
		IToken tkKeywordValues = getToken(RustColorPreferences.KEYWORDS_VALUES.key);
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new LangWhitespaceDetector()));
		
		WordRule wordRule = new WordRule(new JavaWordDetector(), tkOther);
		
		wordRule.addWord("keyword",  tkKeywords);

		wordRule.addWord("null", tkKeywordValues);
		wordRule.addWord("true", tkKeywordValues);
		wordRule.addWord("false", tkKeywordValues);
		
		
		rules.add(wordRule);
		
		setDefaultReturnToken(tkOther);
		return rules;
	}
	
}