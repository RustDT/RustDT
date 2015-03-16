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
package melnorme.lang.ide.ui;

import melnorme.lang.ide.ui.editor.AbstractLangEditor;

import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;

import com.github.rustdt.ide.ui.editor.RustSimpleSourceViewerConfiguration;
import com.github.rustdt.ide.ui.editor.RustSourceViewerConfiguration;

public class EditorSettings_Actual {
	
	public static final String EDITOR_ID = "com.github.rustdt.ide.ui.editor.RustEditor";
	public static final String EDITOR_CONTEXT_ID = "com.github.rustdt.ide.ui.Contexts.Editor";
	
	public static final String EDITOR_CODE_TARGET = "com.github.rustdt.ide.ui.Editor.HyperlinkCodeTarget";
	
	public static RustSourceViewerConfiguration createSourceViewerConfiguration(
			IPreferenceStore preferenceStore, AbstractLangEditor editor) {
		IColorManager colorManager = LangUIPlugin.getInstance().getColorManager();
		return new RustSourceViewerConfiguration(preferenceStore, colorManager, editor);
	}
	
	public static RustSimpleSourceViewerConfiguration createSimpleSourceViewerConfiguration(
			IPreferenceStore preferenceStore, IColorManager colorManager) {
		return new RustSimpleSourceViewerConfiguration(preferenceStore, colorManager);
	}
	
	/* ----------------- actions ----------------- */
	
	public static final String COMMAND_OpenDef_ID = "com.github.rustdt.ide.ui.commands.openDefinition";
	
}
