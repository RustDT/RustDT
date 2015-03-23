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
import melnorme.lang.ide.ui.editor.LangEditorContextMenuContributor;

import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.services.IServiceLocator;

import com.github.rustdt.ide.ui.actions.RustEditorContextMenuContributor;
import com.github.rustdt.ide.ui.editor.RustEditor;
import com.github.rustdt.ide.ui.editor.RustSimpleSourceViewerConfiguration;
import com.github.rustdt.ide.ui.editor.RustSourceViewerConfiguration;

public class EditorSettings_Actual {
	
	public static final String EDITOR_ID = LangUIPlugin.PLUGIN_ID + ".editor.RustEditor";
	public static final String EDITOR_CONTEXT_ID = LangUIPlugin.PLUGIN_ID + ".Contexts.Editor";
	
	public static final String EDITOR_CODE_TARGET = LangUIPlugin.PLUGIN_ID + ".Editor.HyperlinkCodeTarget";
	
	public static RustSourceViewerConfiguration createSourceViewerConfiguration(
			IPreferenceStore preferenceStore, AbstractLangEditor editor) {
		IColorManager colorManager = LangUIPlugin.getInstance().getColorManager();
		return new RustSourceViewerConfiguration(preferenceStore, colorManager, editor);
	}
	
	public static RustSimpleSourceViewerConfiguration createSimpleSourceViewerConfiguration(
			IPreferenceStore preferenceStore, IColorManager colorManager) {
		return new RustSimpleSourceViewerConfiguration(preferenceStore, colorManager);
	}
	
	public static Class<RustEditor> editorKlass() {
		return RustEditor.class;
	}
	
	/* ----------------- actions ----------------- */
	
	public static interface EditorCommandIds {
		
		public static final String OpenDef_ID = LangUIPlugin.PLUGIN_ID + ".commands.openDefinition";
		
		public static final String GoToMatchingBracket = LangUIPlugin.PLUGIN_ID + ".commands.GoToMatchingBracket";
		public static final String ToggleComment = LangUIPlugin.PLUGIN_ID + ".commands.ToggleComment";
		
	}
	
	public static LangEditorContextMenuContributor createCommandsContribHelper(IServiceLocator svcLocator) {
		return new RustEditorContextMenuContributor(svcLocator);
	}
	
}