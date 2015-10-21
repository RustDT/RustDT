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

import melnorme.lang.tooling.parser.lexer.ILexingRule;
import melnorme.lang.utils.parse.ICharacterReader;

public class RustLifetimeLexingRule implements ILexingRule {
	
	@Override
	public boolean doEvaluate(ICharacterReader reader) {
		if(!reader.tryConsume('\'')) {
			return false;
		}
		
		int la = reader.lookahead();
		if(!Character.isJavaIdentifierStart(la)) {
			return false;
		}
		reader.consume();
		
		while(true) { 
			la = reader.lookahead();
			if(Character.isJavaIdentifierPart(la)) {
				reader.consume();
				continue;
			}
			break;
		}
		
		return true;
	}
	
}