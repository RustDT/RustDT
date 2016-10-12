/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Konstantin Salikhov - number lexing rule implementation
 *******************************************************************************/
package com.github.rustdt.tooling.lexer;

import melnorme.lang.tooling.parser.lexer.NumberLexingRule;
import melnorme.lang.utils.parse.ICharacterReader;

// See: https://doc.rust-lang.org/nightly/grammar.html#number-literals
public class RustNumberLexingRule extends NumberLexingRule {
	
	@Override
	protected boolean consumeIntSuffix(ICharacterReader reader) {
		if(reader.lookahead() == 'i') {
			return 
				reader.tryConsume("i8") ||
				reader.tryConsume("i16") ||
				reader.tryConsume("i32") ||
				reader.tryConsume("i64") ||
				reader.tryConsume("isize")
				;
		}
		if(reader.lookahead() == 'u') {
			return 
				reader.tryConsume("u8") ||
				reader.tryConsume("u16") ||
				reader.tryConsume("u32") ||
				reader.tryConsume("u64") ||
				reader.tryConsume("usize")
				;
		}
		return false;
	}
	
	@Override
	protected boolean consumeFloatSuffix(ICharacterReader reader) {
		if(reader.tryConsume("f32") || reader.tryConsume("f64")) {
			return true;
		}
		return false;
	}
	
}