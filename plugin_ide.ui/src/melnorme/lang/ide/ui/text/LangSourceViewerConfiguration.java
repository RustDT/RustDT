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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.ui.text.completion.RustCompletionProposalComputer;

import melnorme.lang.ide.core.TextSettings_Actual.LangPartitionTypes;
import melnorme.lang.ide.core.text.ISourceBufferExt;
import melnorme.lang.ide.core_text.StrictDamagerRepairer;
import melnorme.lang.ide.ui.text.completion.ILangCompletionProposalComputer;
import melnorme.lang.ide.ui.text.completion.LangContentAssistProcessor.ContentAssistCategoriesBuilder;
import melnorme.lang.tooling.LANG_SPECIFIC;

@LANG_SPECIFIC
public class LangSourceViewerConfiguration extends AbstractLangSourceViewerConfiguration {
	
	public LangSourceViewerConfiguration(IPreferenceStore preferenceStore, ISourceBufferExt sourceBuffer, 
			ITextEditor editor) {
		super(preferenceStore, sourceBuffer, editor);
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