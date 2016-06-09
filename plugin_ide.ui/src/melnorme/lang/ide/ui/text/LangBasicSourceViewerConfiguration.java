/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.ide.ui.text;


import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.github.rustdt.ide.ui.text.RustAttributeScanner;
import com.github.rustdt.ide.ui.text.RustCodeScanner;
import com.github.rustdt.ide.ui.text.RustColorPreferences;

import melnorme.lang.ide.core.TextSettings_Actual.LangPartitionTypes;
import melnorme.lang.ide.ui.text.coloring.SingleTokenScanner;
import melnorme.lang.ide.ui.text.coloring.TokenRegistry;

public abstract class LangBasicSourceViewerConfiguration extends AbstractLangBasicSourceViewerConfiguration {
	
	public LangBasicSourceViewerConfiguration(IPreferenceStore preferenceStore) {
		super(preferenceStore);
	}
	
	@Override
	protected AbstractLangScanner createScannerFor(Display current, LangPartitionTypes partitionType,
			TokenRegistry tokenStore) {
		switch (partitionType) {
		case CODE:
			return new RustCodeScanner(tokenStore);
			
		case LINE_COMMENT:
		case BLOCK_COMMENT:
			return new SingleTokenScanner(tokenStore, RustColorPreferences.COMMENTS);
			
		case DOC_LINE_COMMENT:
		case DOC_BLOCK_COMMENT:
			return new SingleTokenScanner(tokenStore, RustColorPreferences.DOC_COMMENTS);
			
		case STRING:
		case RAW_STRING:
			return new SingleTokenScanner(tokenStore, RustColorPreferences.STRINGS);
			
		case CHARACTER:
			return new SingleTokenScanner(tokenStore, RustColorPreferences.CHARACTER);
		case LIFETIME:
			return new SingleTokenScanner(tokenStore, RustColorPreferences.LIFETIME);
		case ATTRIBUTE:
			return new RustAttributeScanner(tokenStore);
		}
		
		throw assertFail();
	}
	
}