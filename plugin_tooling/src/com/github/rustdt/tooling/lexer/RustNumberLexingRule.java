/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Konstantin Salikhov - number lexing rule implementation
 *******************************************************************************/
package com.github.rustdt.tooling.lexer;

import melnorme.lang.tooling.parser.lexer.CommonLexingRule;
import melnorme.lang.tooling.parser.lexer.ICharacterReader;
import melnorme.lang.tooling.parser.lexer.ILexingRule;

public class RustNumberLexingRule extends CommonLexingRule implements ILexingRule {

	@Override
	public boolean evaluate(ICharacterReader reader) {

		if (!checkPrefix(reader)) {
			return false;
		}

		boolean hasPrefix = consumeIntPrefix(reader);
		while (hasPrefix && reader.consume('_')) {
			// JUST SKIP THEM
		}

		if (!consumeDigit(reader)) {
			return false;
		}

		while (consumeDigit(reader) || reader.consume('_')) {
			// JUST SKIP THEM
		}

		if (!consumeIntSuffix(reader)) {
			if (reader.consume('.')) {
				if (reader.lookahead() == '.') {
					reader.unread();
				} else {
					if (!consumeDigit(reader)) {
						return false;
					}
					while (consumeDigit(reader) || reader.consume('_')) {
						// JUST SKIP THEM
					}
					consumeFloatSuffix(reader);
				}
			}
		}

		int postfix = reader.lookahead();
		if (Character.isAlphabetic(postfix)) {
			return false;
		}

		return true;
	}

	private static boolean checkPrefix(ICharacterReader reader) {
		int prefix; 
		int behind = 0;
		while(true) {
			prefix = reader.lookahead();
			if (Character.isDigit(prefix)) {
				reader.unread();
				behind++;
				continue;
			}
			break;
		}
		
		for (int i = 0; i < behind; i++) {
			reader.read();
		}
		
		if (Character.isAlphabetic(prefix) || '_' == prefix) {
			return false;
		}
		
		return true;
	}

	private static boolean consumeDigit(ICharacterReader reader) {
		int c = reader.lookahead();
		if (c >= '0' && c <= '9') {
			reader.read();
			return true;
		}
		return false;
	}

	private static boolean consumeIntPrefix(ICharacterReader reader) {
		if (reader.consume('0')) {
			if (reader.consume('x') || reader.consume('o') || reader.consume('b')) {
				return true;
			} else {
				reader.unread();
			}
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