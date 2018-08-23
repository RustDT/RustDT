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

import static melnorme.utilbox.core.CoreUtil.areEqual;

import melnorme.lang.tooling.parser.lexer.CharacterReader_SubReader;
import melnorme.lang.tooling.parser.lexer.WordLexerRule;
import melnorme.lang.utils.parse.ICharacterReader;
import melnorme.lang.utils.parse.LexingUtils;

/**
 * A lexer rule used for coloring purposes.
 */
public class RustWordLexerRule extends WordLexerRule<RustColoringTokens> {
	
	public static final String[] keywords_control = { 
			"abstract", "alignof", "as", "become", "box", "break", "const", "continue", "crate", 
			"do", "dyn", "else", "enum", "extern", "final", "fn", "for", "if", "impl", "in", "let", 
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
	
	public RustWordLexerRule() {
		super(RustColoringTokens.WS, RustColoringTokens.WORD);
		
		addKeywords(RustColoringTokens.KEYWORD, RustWordLexerRule.keywords_control);
		addKeywords(RustColoringTokens.KEYWORD_BOOL, RustWordLexerRule.keywords_boolean_lit);
		addKeywords(RustColoringTokens.KEYWORD_SELF, RustWordLexerRule.keywords_self);
	}
	
	@Override
	public RustColoringTokens doEvaluateToken(ICharacterReader reader) {
		if(reader.tryConsume('?')) {
			return RustColoringTokens.TRY_OP;
		}
		
		RustColoringTokens result = super.doEvaluateToken(reader);
		
		if(result == null) {
			if(rustNumberLexingRule.tryMatch(reader)) {
				return RustColoringTokens.NUMBER;
			}
		}
		
		if(result != defaultWordToken) {
			return result;
		}
		// We found a word then. Let's see if it's a macro invocation
		
		CharacterReader_SubReader subReader = new CharacterReader_SubReader(reader); 
		LexingUtils.skipWhitespace(subReader);
		if(subReader.tryConsume('!')) {
			
			int afterWS = LexingUtils.countWhitespace(subReader);
			int lookahead = subReader.lookahead(afterWS);
			if(areEqual(lastEvaluatedWord, "macro_rules")) {
				subReader.consumeInParentReader();
				return RustColoringTokens.MACRO_RULES;
			}
			if(lookahead == '(' || lookahead == '[') {
				subReader.consumeInParentReader();
				return RustColoringTokens.MACRO_CALL;
			}
		}
		return result;
	}
	
}