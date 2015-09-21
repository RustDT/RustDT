/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
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

import org.eclipse.jface.preference.IPreferenceStore;

import com.github.rustdt.ide.ui.text.RustColorPreferences;

import melnorme.lang.ide.ui.text.coloring.AbstractSourceColoringConfigurationBlock;
import melnorme.util.swt.jface.LabeledTreeElement;

public class SourceColoringConfigurationBlock extends AbstractSourceColoringConfigurationBlock {
	
	protected static final LabeledTreeElement[] treeElements = array(
		new SourceColoringCategory("Source", array(
			new SourceColoringElement("Default", RustColorPreferences.DEFAULT.key),
			new SourceColoringElement("Keywords", RustColorPreferences.KEYWORDS.key),
			new SourceColoringElement("Keywords - Literals", RustColorPreferences.KEYWORDS_VALUES.key),
			new SourceColoringElement("Macro Invocation", RustColorPreferences.MACRO_CALL.key),
			new SourceColoringElement("Strings", RustColorPreferences.STRINGS.key),
			new SourceColoringElement("Characters", RustColorPreferences.CHARACTER.key),
			new SourceColoringElement("Numbers", RustColorPreferences.NUMBERS.key),
			new SourceColoringElement("Lifetime", RustColorPreferences.LIFETIME.key),
			new SourceColoringElement("Attribute", RustColorPreferences.ATTRIBUTE.key)
		)),
		new SourceColoringCategory("Comments", array(
			new SourceColoringElement("Comment", RustColorPreferences.COMMENTS.key),
			new SourceColoringElement("Documentation Comment", RustColorPreferences.DOC_COMMENTS.key)
		))
	);
	
	public SourceColoringConfigurationBlock(IPreferenceStore store) {
		super(store);
	}
	
	@Override
	protected LabeledTreeElement[] getTreeElements() {
		return treeElements;
	}
	
	private static final String PREVIEW_FILE_NAME = "SourceColoringPreviewFile.lang";
	
	@Override
	protected InputStream getPreviewContentAsStream() {
		return getClass().getResourceAsStream(PREVIEW_FILE_NAME);
	}
	
}