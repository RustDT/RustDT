/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling.lexer;

import melnorme.lang.tooling.parser.lexer.CommonLexingRule;
import melnorme.lang.tooling.parser.lexer.ICharacterReader;
import melnorme.lang.tooling.parser.lexer.ILexingRule;

public class RustLifetimeLexingRule extends CommonLexingRule implements ILexingRule {
	
	@Override
	public boolean evaluate(ICharacterReader reader) {
		if(!reader.consume('\'')) {
			return false;
		}
		
		int la = reader.lookahead();
		if(!Character.isJavaIdentifierStart(la)) {
			return false;
		}
		reader.read();
		
		while(true) { 
			la = reader.lookahead();
			if(Character.isJavaIdentifierPart(la)) {
				reader.read();
				continue;
			}
			break;
		}
		
		return true;
	}
	
}