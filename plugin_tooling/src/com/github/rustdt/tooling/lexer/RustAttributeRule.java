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
import melnorme.lang.utils.parse.LexingUtils;

public class RustAttributeRule extends LexingUtils implements ILexingRule {
	
	@Override
	public boolean doEvaluate(ICharacterReader reader) {
		if(!reader.tryConsume("#[") && !reader.tryConsume("#![")) {
			return false;
		}
		
		while(!reader.tryConsume("]")) {
			
			if(reader.lookaheadIsEOF()) {
				return true;
			}
			
			if(reader.tryConsume('"')) {
				consumeDelimitedString(reader);
				continue;
			}
			reader.consume();
		}
		
		return true;
	}
	
	protected void consumeDelimitedString(ICharacterReader reader) {
		advanceDelimitedString(reader, '"', '\\');
	}
	
}