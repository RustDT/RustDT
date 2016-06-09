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
package melnorme.lang.ide.ui.text;

import static melnorme.utilbox.core.CoreUtil.array;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;

import com.github.rustdt.ide.ui.text.completion.RustCompletionProposalComputer;

import melnorme.lang.ide.core.TextSettings_Actual.LangPartitionTypes;
import melnorme.lang.ide.core.text.StrictDamagerRepairer;
import melnorme.lang.ide.ui.LangUIPlugin_Actual;
import melnorme.lang.ide.ui.editor.structure.AbstractLangStructureEditor;
import melnorme.lang.ide.ui.text.completion.ILangCompletionProposalComputer;
import melnorme.lang.ide.ui.text.completion.LangContentAssistProcessor.ContentAssistCategoriesBuilder;
import melnorme.lang.tooling.LANG_SPECIFIC;

@LANG_SPECIFIC
public class LangSourceViewerConfiguration extends AbstractLangSourceViewerConfiguration {
	
	public LangSourceViewerConfiguration(IPreferenceStore preferenceStore, AbstractLangStructureEditor editor) {
		super(preferenceStore, editor);
	}
	
	@Override
	protected DefaultDamagerRepairer getDamagerRepairer(AbstractLangScanner scanner, String contentType) {
		if(contentType.equals(LangPartitionTypes.ATTRIBUTE.getId())) {
			return new StrictDamagerRepairer(scanner);
		}
		return super.getDamagerRepairer(scanner, contentType);
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