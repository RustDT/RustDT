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
package com.github.rustdt.tooling.lexer;

import melnorme.lang.tooling.parser.lexer.WordLexerRule;

public class RustWordLexerRule<TOKEN> extends WordLexerRule<TOKEN> {
	
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
	
	public RustWordLexerRule(TOKEN whitespaceToken, TOKEN tkKeywords, TOKEN tkKeyword_Literals, TOKEN tkWord) {
		super(whitespaceToken, tkWord);
		
		addKeywords(tkKeywords, keywords_control);
		addKeywords(tkKeyword_Literals, keywords_values);
	}
	
}