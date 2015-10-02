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

import static melnorme.utilbox.core.CoreUtil.array;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.services.IServiceLocator;

import com.github.rustdt.ide.ui.actions.RustEditorContextMenuContributor;
import com.github.rustdt.ide.ui.editor.RustEditor;
import com.github.rustdt.ide.ui.editor.RustSimpleSourceViewerConfiguration;
import com.github.rustdt.ide.ui.text.RustColorPreferences;

import melnorme.lang.ide.core.TextSettings_Actual.LangPartitionTypes;
import melnorme.lang.ide.ui.editor.LangEditorContextMenuContributor;
import melnorme.lang.ide.ui.editor.text.EditorPrefConstants_Common;
import melnorme.util.swt.jface.text.ColorManager2;

public class EditorSettings_Actual {
	
	public static final String EDITOR_ID = LangUIPlugin.PLUGIN_ID + ".editor.RustEditor";
	public static final String EDITOR_CONTEXT_ID = LangUIPlugin.PLUGIN_ID + ".Contexts.Editor";
	
	public static final String EDITOR_CODE_TARGET = LangUIPlugin.PLUGIN_ID + ".Editor.HyperlinkCodeTarget";
	
	public static RustSimpleSourceViewerConfiguration createSimpleSourceViewerConfiguration(
			IPreferenceStore preferenceStore, ColorManager2 colorManager) {
		return new RustSimpleSourceViewerConfiguration(preferenceStore, colorManager);
	}
	
	public static Class<RustEditor> editorKlass() {
		return RustEditor.class;
	}
	
	public static interface EditorPrefConstants extends EditorPrefConstants_Common {
		
	}
	
	public static final String TEMPLATE_CONTEXT_TYPE_ID = LangUIPlugin.PLUGIN_ID + ".TemplateContextType";
	
	public static final String CODE_DEFAULT_COLOR = RustColorPreferences.DEFAULT.key;
	
	public static SourceViewerConfiguration createTemplateEditorSourceViewerConfiguration(
			IPreferenceStore store, final IContentAssistProcessor templateCAP) {
		ColorManager2 colorManager = LangUIPlugin.getInstance().getColorManager();
		return new RustSimpleSourceViewerConfiguration(store, colorManager) {
			@Override
			public ContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
				return setupSimpleContentAssistant(templateCAP, array(
					LangPartitionTypes.CODE.getId(), 
					LangPartitionTypes.LINE_COMMENT.getId(), 
					LangPartitionTypes.BLOCK_COMMENT.getId(), 
					LangPartitionTypes.DOC_LINE_COMMENT.getId(),
					LangPartitionTypes.DOC_BLOCK_COMMENT.getId(), 
					LangPartitionTypes.LIFETIME.getId(),
					LangPartitionTypes.ATTRIBUTE.getId()
				));
			}
			
		};
	}
	
	/* ----------------- actions ----------------- */
	
	public static interface EditorCommandIds {
		
		public static final String OpenDef_ID = LangUIPlugin.PLUGIN_ID + ".commands.openDefinition";
		
		public static final String GoToMatchingBracket = LangUIPlugin.PLUGIN_ID + ".commands.GoToMatchingBracket";
		public static final String ToggleComment = LangUIPlugin.PLUGIN_ID + ".commands.ToggleComment";
		
		public static final String QuickOutline = LangUIPlugin.PLUGIN_ID + ".commands.QuickOutline";
		
	}
	
	public static LangEditorContextMenuContributor createCommandsContribHelper(IServiceLocator svcLocator) {
		return new RustEditorContextMenuContributor(svcLocator);
	}
	
}
