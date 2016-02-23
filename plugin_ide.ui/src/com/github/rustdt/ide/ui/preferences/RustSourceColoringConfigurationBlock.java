/*******************************************************************************
 * Copyright (c) 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.preferences;

import static melnorme.utilbox.core.CoreUtil.array;

import java.io.InputStream;

import com.github.rustdt.ide.ui.text.RustColorPreferences;

import melnorme.lang.ide.ui.preferences.common.PreferencesPageContext;
import melnorme.lang.ide.ui.text.coloring.AbstractSourceColoringConfigurationBlock;
import melnorme.util.swt.jface.LabeledTreeElement;

public class RustSourceColoringConfigurationBlock extends AbstractSourceColoringConfigurationBlock {
	
	public RustSourceColoringConfigurationBlock(PreferencesPageContext prefContext) {
		super(prefContext);
	}
	
	@Override
	protected LabeledTreeElement[] createTreeElements() {
		return array(
			new SourceColoringCategory("Source", array(
				new SourceColoringElement("Default", RustColorPreferences.DEFAULT),
				new SourceColoringElement("Keywords", RustColorPreferences.KEYWORDS),
				new SourceColoringElement("Keywords - true/false", RustColorPreferences.KEYWORDS_BOOLEAN_LIT),
				new SourceColoringElement("Keywords - self", RustColorPreferences.KEYWORDS_SELF),
				new SourceColoringElement("Macro Invocation", RustColorPreferences.MACRO_CALL),
				new SourceColoringElement("Strings", RustColorPreferences.STRINGS),
				new SourceColoringElement("Characters", RustColorPreferences.CHARACTER),
				new SourceColoringElement("Numbers", RustColorPreferences.NUMBERS),
				new SourceColoringElement("Lifetime", RustColorPreferences.LIFETIME),
				new SourceColoringElement("Attribute", RustColorPreferences.ATTRIBUTE)
			)),
			new SourceColoringCategory("Comments", array(
				new SourceColoringElement("Comment", RustColorPreferences.COMMENTS),
				new SourceColoringElement("Documentation Comment", RustColorPreferences.DOC_COMMENTS)
			))
		);
	}
	
	private static final String PREVIEW_FILE_NAME = "SourceColoringPreviewFile.lang";
	
	@Override
	protected InputStream getPreviewContentAsStream() {
		return getClass().getResourceAsStream(PREVIEW_FILE_NAME);
	}
	
}