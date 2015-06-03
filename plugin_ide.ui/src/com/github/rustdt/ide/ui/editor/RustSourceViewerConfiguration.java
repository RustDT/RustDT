/*******************************************************************************
 * Copyright (c) 2014, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.editor;

import static melnorme.utilbox.core.CoreUtil.array;
import melnorme.lang.ide.core.TextSettings_Actual.LangPartitionTypes;
import melnorme.lang.ide.ui.LangUIPlugin_Actual;
import melnorme.lang.ide.ui.editor.AbstractLangEditor;
import melnorme.lang.ide.ui.text.AbstractLangSourceViewerConfiguration;
import melnorme.lang.ide.ui.text.completion.ILangCompletionProposalComputer;
import melnorme.lang.ide.ui.text.completion.LangContentAssistProcessor.ContentAssistCategoriesBuilder;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.source.ISourceViewer;

import _org.eclipse.cdt.ui.text.IColorManager;

import com.github.rustdt.ide.ui.text.RustAttributeScanner;
import com.github.rustdt.ide.ui.text.RustCodeScanner;
import com.github.rustdt.ide.ui.text.RustColorPreferences;
import com.github.rustdt.ide.ui.text.completion.RustCompletionProposalComputer;

public class RustSourceViewerConfiguration extends AbstractLangSourceViewerConfiguration {
	
	public RustSourceViewerConfiguration(IPreferenceStore preferenceStore, IColorManager colorManager,
			AbstractLangEditor editor) {
		super(preferenceStore, colorManager, editor);
	}
	
	@Override
	protected void createScanners() {
		addScanner(new RustCodeScanner(getTokenStoreFactory()), IDocument.DEFAULT_CONTENT_TYPE);
		
		addScanner(createSingleTokenScanner(RustColorPreferences.COMMENTS.key), 
			LangPartitionTypes.LINE_COMMENT.getId());
		addScanner(createSingleTokenScanner(RustColorPreferences.COMMENTS.key), 
			LangPartitionTypes.BLOCK_COMMENT.getId());
		
		addScanner(createSingleTokenScanner(RustColorPreferences.DOC_COMMENTS.key), 
			LangPartitionTypes.DOC_LINE_COMMENT.getId());
		addScanner(createSingleTokenScanner(RustColorPreferences.DOC_COMMENTS.key), 
			LangPartitionTypes.DOC_BLOCK_COMMENT.getId());
		
		addScanner(createSingleTokenScanner(RustColorPreferences.STRINGS.key), 
			LangPartitionTypes.STRING.getId(),
			LangPartitionTypes.RAW_STRING.getId()
			);
		addScanner(createSingleTokenScanner(RustColorPreferences.CHARACTER.key), 
			LangPartitionTypes.CHARACTER.getId());
		addScanner(createSingleTokenScanner(RustColorPreferences.LIFETIME.key), 
			LangPartitionTypes.LIFETIME.getId());
		addScanner(new RustAttributeScanner(getTokenStoreFactory()), 
			LangPartitionTypes.ATTRIBUTE.getId());
	}
	
	@Override
	protected IInformationProvider getInformationProvider(String contentType) {
		return null;
	}
	
	/* ----------------- Modification operations ----------------- */
	
	@Override
	protected String getToggleCommentPrefix() {
		return "//";
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if(IDocument.DEFAULT_CONTENT_TYPE.equals(contentType)) {
			return array(LangUIPlugin_Actual.createAutoEditStrategy(sourceViewer, contentType));
		} else {
			return super.getAutoEditStrategies(sourceViewer, contentType);
		}
	}
	
	/* ----------------- Content Assist ----------------- */
	
	@Override
	protected ContentAssistCategoriesBuilder getContentAssistCategoriesProvider() {
		return new ContentAssistCategoriesBuilder() {
			@Override
			protected ILangCompletionProposalComputer createDefaultSymbolsProposalComputer() {
				return new RustCompletionProposalComputer();
			}
		};
	}
	
}