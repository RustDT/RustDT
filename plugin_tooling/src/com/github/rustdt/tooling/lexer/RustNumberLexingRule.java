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

		boolean hasOctPrefix = false;
		boolean hasHexPrefix = false;
		boolean hasBinPrefix = false;

		hasOctPrefix = consumeOctPrefix(reader);
		if (!hasOctPrefix) {
			hasHexPrefix = consumeHexPrefix(reader);
			if (!hasHexPrefix) {
				hasBinPrefix = consumeBinPrefix(reader);
			}
		}

		boolean hasPrefix = hasOctPrefix || hasHexPrefix || hasBinPrefix;
		while (hasPrefix && reader.consume('_')) {
			// JUST SKIP THEM
		}

		reader.consume('-');

		int radix = hasHexPrefix ? 16 : (hasOctPrefix ? 8 : (hasBinPrefix ? 2 : 10));

		if (!consumeDigit(reader, radix)) {
			return false;
		}

		while (consumeDigit(reader, radix) || reader.consume('_')) {
			// JUST SKIP THEM
		}

		if (!consumeIntSuffix(reader)) {
			if (reader.consume('.')) {
				if (reader.lookahead() == '.') {
					reader.unread();
				} else {
					if (hasPrefix) {
						return false;
					}
					
					if (!consumeDigit(reader, radix)) {
						return false;
					}
					while (consumeDigit(reader, radix) || reader.consume('_')) {
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
		while (true) {
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

	private static boolean consumeHexPrefix(ICharacterReader reader) {
		if (reader.consume('0')) {
			if (reader.consume('x')) {
				return true;
			} else {
				reader.unread();
			}
		}
		return false;
	}

	private static boolean consumeOctPrefix(ICharacterReader reader) {
		if (reader.consume('0')) {
			if (reader.consume('o')) {
				return true;
			} else {
				reader.unread();
			}
		}
		return false;
	}

	private static boolean consumeBinPrefix(ICharacterReader reader) {
		if (reader.consume('0')) {
			if (reader.consume('b')) {
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