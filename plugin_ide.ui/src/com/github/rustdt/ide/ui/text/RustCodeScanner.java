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
 * Rust scanner for the code partition
 */
public class RustCodeScanner extends AbstractLangScanner {
	
	private static String tokenPrefProperties[] = new String[] {
		RustColorPreferences.DEFAULT.key,
		RustColorPreferences.KEYWORDS.key,
		RustColorPreferences.KEYWORDS_VALUES.key,
	};
	
	
	public static final String[] keywords_control = { 
			"abstract", "alignof", "as", "be", "box", "break", "const", "continue", "crate", 
			"do", "else", "enum", "extern", "final", "fn", "for", "if", "impl", "in", "let", 
			"loop", "macro", "match", "mod", "move", "mut", "offsetof", "override", 
			"priv", "pub", "pure", "ref", "return", "sizeof", "static", "struct", 
			"trait", "type", "typeof", "unsafe", "unsized", "use", "virtual", "where", "while", "yield" 
	};
	public static final String[] keywords_values = { 
			"true", "false", "self", "super", "null" // Is null actually used?
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
		
		for (String keyword : keywords_control) {
			wordRule.addWord(keyword,  tkKeywords);
		}
		for (String keyword : keywords_values) {
			wordRule.addWord(keyword,  tkKeywordValues);
		}
		
		rules.add(wordRule);
		
		setDefaultReturnToken(tkOther);
		return rules;
	}
	
}