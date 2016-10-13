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

import melnorme.lang.tooling.parser.lexer.IPredicateLexingRule;
import melnorme.lang.utils.parse.ICharacterReader;

public class RustRawStringLiteralRule implements IPredicateLexingRule {
	
	@Override
	public boolean doEvaluate(ICharacterReader reader) {
		reader.tryConsume('b');
		
		if(!reader.tryConsume('r')) {
			return false;
		}
		
		int terminatorHashCount = 0;
		
		while(reader.tryConsume('#')) {
			terminatorHashCount++;
		}
		
		if(!reader.tryConsume('"')) {
			return false;
		}
		
		while(reader.hasCharAhead()) {
			char ch = reader.consume();
			if(ch != '"') {
				continue;
			}
			
			int consumedHash = 0;
			while(consumedHash < terminatorHashCount && reader.tryConsume('#')) {
				consumedHash++;
			}
			
			if(consumedHash == terminatorHashCount) {
				return true;
			}
			
		}
		
		return true;
	}
	
}