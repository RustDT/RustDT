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

import melnorme.lang.tooling.parser.lexer.CharacterReader_SubReader;
import melnorme.lang.tooling.parser.lexer.WordLexerRule;
import melnorme.lang.utils.parse.ICharacterReader;
import melnorme.lang.utils.parse.LexingUtils;

public class RustWordLexerRule<TOKEN> extends WordLexerRule<TOKEN> {
	
	public static final String[] keywords_control = { 
			"abstract", "alignof", "as", "become", "box", "break", "const", "continue", "crate", 
			"do", "else", "enum", "extern", "final", "fn", "for", "if", "impl", "in", "let", 
			"loop", "macro", "match", "mod", "move", "mut", "offsetof", "override", 
			"priv", "proc", "pub", "pure", "ref", "return", "sizeof", "static", "struct", 
			"trait", "type", "typeof", "unsafe", "unsized", "use", "virtual", "where", "while", "yield" 
	};
	public static final String[] keywords_boolean_lit = { 
			"true", "false",
	};
	public static final String[] keywords_self = { 
			"self", "Self", "super"
	};
	
	/* -----------------  ----------------- */
	
	protected final RustNumberLexingRule rustNumberLexingRule = new RustNumberLexingRule();
	
	protected final TOKEN macroCall;
	protected final TOKEN numberLiteral;
	
	public RustWordLexerRule(
			TOKEN whitespaceToken, 
			TOKEN keywords, 
			TOKEN keywords_booleanLiteral, 
			TOKEN keywords_self, 
			TOKEN word,
			TOKEN macroCall, 
			TOKEN numberLiteral
			) {
		super(whitespaceToken, word);
		this.macroCall = macroCall;
		this.numberLiteral = numberLiteral;
		
		addKeywords(keywords, RustWordLexerRule.keywords_control);
		addKeywords(keywords_booleanLiteral, RustWordLexerRule.keywords_boolean_lit);
		addKeywords(keywords_self, RustWordLexerRule.keywords_self);
	}
	
	@Override
	public TOKEN doEvaluateToken(ICharacterReader reader) {
		TOKEN result = super.doEvaluateToken(reader);
		
		if(result == null) {
			if(rustNumberLexingRule.tryMatch(reader)) {
				return numberLiteral;
			}
		}
		
		if(result != defaultWordToken) {
			return result;
		}
		// We found a word then. Let's see if it's a macro invocation
		
		CharacterReader_SubReader subReader = new CharacterReader_SubReader(reader); 
		LexingUtils.skipWhitespace(subReader);
		if(subReader.tryConsume('!')) {
			subReader.consumeInParentReader();
			return macroCall;
		}
		return result;
	}
	
}