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
package com.github.rustdt.ide.ui.text;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.PatternRule;

import _org.eclipse.cdt.ui.text.ITokenStoreFactory;
import melnorme.lang.ide.ui.text.AbstractLangScanner;
import melnorme.utilbox.collections.ArrayList2;

/**
 * Scanner for Attribute partion
 */
public class RustAttributeScanner extends AbstractLangScanner {
	
	private static String tokenPrefProperties[] = new String[] {
		RustColorPreferences.ATTRIBUTE.key,
		RustColorPreferences.STRINGS.key,
	};
	
	public RustAttributeScanner(ITokenStoreFactory factory) {
		super(factory.createTokenStore(tokenPrefProperties));
	}
	
	@Override
	protected void initRules(ArrayList2<IRule> rules) {
		setDefaultReturnToken(getToken(RustColorPreferences.ATTRIBUTE.key));
		
		IToken tkString = getToken(RustColorPreferences.STRINGS.key);
		
		rules.add(new PatternRule("\"", "\"", tkString, '\\', false, true));
	}
	
}