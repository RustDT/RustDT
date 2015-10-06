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
package com.github.rustdt.ide.ui.text;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.PatternRule;

import melnorme.lang.ide.ui.text.AbstractLangScanner;
import melnorme.lang.ide.ui.text.coloring.StylingPreferences;
import melnorme.lang.ide.ui.text.coloring.TokenRegistry;
import melnorme.utilbox.collections.ArrayList2;

/**
 * Scanner for Attribute partion
 */
public class RustAttributeScanner extends AbstractLangScanner {
	
	public RustAttributeScanner(TokenRegistry tokenStore, StylingPreferences stylingPrefs) {
		super(tokenStore, stylingPrefs);
	}
	
	@Override
	protected void initRules(ArrayList2<IRule> rules) {
		setDefaultReturnToken(getToken(RustColorPreferences.ATTRIBUTE));
		
		IToken tkString = getToken(RustColorPreferences.STRINGS);
		
		rules.add(new PatternRule("\"", "\"", tkString, '\\', false, true));
	}
	
}