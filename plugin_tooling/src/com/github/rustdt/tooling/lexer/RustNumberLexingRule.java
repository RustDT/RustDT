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

import melnorme.lang.tooling.parser.lexer.CharacterReaderWrapper;
import melnorme.lang.tooling.parser.lexer.CommonLexingRule;
import melnorme.lang.tooling.parser.lexer.ICharacterReader;
import melnorme.lang.tooling.parser.lexer.ILexingRule;

public class RustNumberLexingRule extends CommonLexingRule implements ILexingRule {

	@Override
	public boolean evaluate(ICharacterReader reader) {
		reader = new CharacterReaderWrapper(reader);
		
		boolean sucess = doEvaluate(reader);
		if(!sucess) {
			reader.reset();
		}
		return sucess;
	}
	
	// See: https://doc.rust-lang.org/nightly/grammar.html#number-literals
	protected boolean doEvaluate(ICharacterReader reader) {
		
		reader.consume('-');
		
		int radix = 10;
		
		if(reader.consume('0')) {
			if(reader.consume('b')) {
				radix = 2;
			} 
			else if(reader.consume('o')) {
				radix = 8;
			}
			else if(reader.consume('x')) {
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
		
		if (!hasPrefix && reader.consume('.')) {
			if (reader.lookahead() == '.') {
				reader.unread();
			} else {
				
				consumeDigits(reader, radix);
				// TODO: float exponent
				consumeFloatSuffix(reader);
			}
		}
		
		return true;
	}

	protected void consumeDigits(ICharacterReader reader, int radix) {
		while (consumeDigit(reader, radix) || reader.consume('_')) {
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
			reader.read();
			return true;
		}
		return false;
	}

	private static boolean consumeIntSuffix(ICharacterReader reader) {
		if (reader.consume('i') || reader.consume('u')) {
			if (reader.consume('8')) {
				return true;
			} else if (reader.consume('1')) {
				if (reader.consume('6')) {
					return true;
				} else {
					reader.unread();
					reader.unread();
				}
			} else if (reader.consume('3')) {
				if (reader.consume('2')) {
					return true;
				} else {
					reader.unread();
					reader.unread();
				}
			} else if (reader.consume('6')) {
				if (reader.consume('4')) {
					return true;
				} else {
					reader.unread();
					reader.unread();
				}
			} else {
				reader.unread();
			}
		}
		return false;
	}

	private static boolean consumeFloatSuffix(ICharacterReader reader) {
		if (reader.consume('f')) {
			if (reader.consume('3')) {
				if (reader.consume('2')) {
					return true;
				} else {
					reader.unread();
					reader.unread();
				}
			} else if (reader.consume('6')) {
				if (reader.consume('4')) {
					return true;
				} else {
					reader.unread();
					reader.unread();
				}
			} else {
				reader.unread();
			}
		}
		return false;
	}

}