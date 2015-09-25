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

import melnorme.lang.tooling.parser.lexer.ILexingRule;
import melnorme.lang.utils.parse.ICharacterReader;

public class RustNumberLexingRule implements ILexingRule {

	// See: https://doc.rust-lang.org/nightly/grammar.html#number-literals
	@Override
	public boolean doEvaluate(ICharacterReader reader) {
		
		reader.tryConsume('-');
		
		int radix = 10;
		
		if(reader.tryConsume('0')) {
			if(reader.tryConsume('b')) {
				radix = 2;
			} 
			else if(reader.tryConsume('o')) {
				radix = 8;
			}
			else if(reader.tryConsume('x')) {
				radix = 16;
			}
		} else {
			if(!consumeDigit(reader, radix)) {
				return false;
			}
		}
		
		consumeDigits(reader, radix);
		
		if (consumeIntSuffix(reader)) {
			return true;
		}
		
		boolean hasPrefix = radix != 10;
		
		if(!hasPrefix && reader.lookahead() == '.' && reader.lookahead(1) != '.') {
			reader.consume();
			consumeDigits(reader, radix);
			// TODO: float exponent
			consumeFloatSuffix(reader);
		}
		
		return true;
	}

	protected void consumeDigits(ICharacterReader reader, int radix) {
		while (consumeDigit(reader, radix) || reader.tryConsume('_')) {
			// JUST SKIP THEM
		}
	}

	private static boolean consumeDigit(ICharacterReader reader, int radix) {
		int c = reader.lookahead();

		boolean hex = (radix == 16) && ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
		boolean dec = (radix == 10) && (c >= '0' && c <= '9');
		boolean oct = (radix == 8) && (c >= '0' && c <= '7');
		boolean bin = (radix == 2) && (c >= '0' && c <= '1');

		if (hex || dec || oct || bin) {
			reader.consume();
			return true;
		}
		return false;
	}

	private static boolean consumeIntSuffix(ICharacterReader reader) {
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

	private static boolean consumeFloatSuffix(ICharacterReader reader) {
		if(reader.tryConsume("f32") || reader.tryConsume("f64")) {
			return true;
		}
		return false;
	}
	
}