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

import melnorme.lang.tooling.parser.lexer.CharacterLexingRule;
import melnorme.lang.utils.parse.ICharacterReader;

public class RustCharacterLexingRule extends CharacterLexingRule {
	
	@Override
	protected boolean consumeStart(ICharacterReader reader) {
		if(reader.tryConsume("'")) {
			return true;
		}
		if(reader.tryConsume("b'")) {
			return true;
		}
		return false;
	}
	
}