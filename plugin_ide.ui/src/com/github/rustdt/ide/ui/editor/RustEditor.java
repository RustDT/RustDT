/*******************************************************************************
 * Copyright (c) 2014, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.editor;

import melnorme.lang.ide.ui.LangUIPlugin;
import melnorme.lang.ide.ui.editor.AbstractLangEditor;
import melnorme.lang.ide.ui.editor.text.LangPairMatcher;
import melnorme.lang.ide.ui.text.AbstractLangSourceViewerConfiguration;

import org.eclipse.cdt.ui.text.IColorManager;

public class RustEditor extends AbstractLangEditor {
	
	@Override
	protected LangPairMatcher init_createBracketMatcher() {
		return new LangPairMatcher("{}[]()".toCharArray());
	}
	
	@Override
	protected AbstractLangSourceViewerConfiguration createSourceViewerConfiguration() {
		IColorManager colorManager = LangUIPlugin.getInstance().getColorManager();
		return new RustSourceViewerConfiguration(getPreferenceStore(), colorManager, this);
	}
	
}