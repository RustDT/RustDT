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
package com.github.rustdt.ide.core.text;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.Token;

import com.github.rustdt.tooling.lexer.RustAttributeRule;
import com.github.rustdt.tooling.lexer.RustCharacterLexingRule;
import com.github.rustdt.tooling.lexer.RustLifetimeLexingRule;

import melnorme.lang.ide.core.TextSettings_Actual.LangPartitionTypes;
import melnorme.lang.ide.core.text.LangPartitionScanner;
import melnorme.utilbox.collections.ArrayList2;

public class RustPartitionScanner extends LangPartitionScanner {
	
	public RustPartitionScanner() {
	}
	
	@Override
	protected void initPredicateRules(ArrayList2<IPredicateRule> rules) {
		addStandardRules(rules, 
			LangPartitionTypes.LINE_COMMENT.getId(), 
			LangPartitionTypes.BLOCK_COMMENT.getId(),
			LangPartitionTypes.DOC_LINE_COMMENT.getId(),
			LangPartitionTypes.DOC_BLOCK_COMMENT.getId(),
			null
		);
		
		rules.add(new PredicateRule_Adapter(LangPartitionTypes.ATTRIBUTE.getId(), new RustAttributeRule()));
		
		rules.add(new PredicateRule_Adapter(LangPartitionTypes.CHARACTER.getId(), new RustCharacterLexingRule()));
		
		// Note: this rule must be after character rule
		rules.add(new PredicateRule_Adapter(LangPartitionTypes.LIFETIME.getId(), new RustLifetimeLexingRule()));
		
		IToken tkString = new Token(LangPartitionTypes.STRING.getId());
		rules.add(new PatternRule("\"", "\"", tkString, '\\', false, true));
		rules.add(new PatternRule("b\"", "\"", tkString, '\\', false, true));
		// Raw strings have different terminate, so they need to a different partition type.
		IToken tkRawString = new Token(LangPartitionTypes.RAW_STRING.getId());
		rules.add(new PatternRule("r##\"", "\"##", tkRawString, NO_ESCAPE_CHAR, false, true));
		rules.add(new PatternRule("br##\"", "\"##", tkRawString, NO_ESCAPE_CHAR, false, true));
	}
	
}