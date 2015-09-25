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
package com.github.rustdt.ide.ui.editor;

import melnorme.lang.ide.ui.editor.EditorUtils.OpenNewEditorMode;
import melnorme.lang.ide.ui.editor.LangHyperlinkDetector;
import melnorme.lang.tooling.ast.SourceRange;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.ui.actions.RustOpenDefinitionOperation;

public class RustHyperlinkDetector extends LangHyperlinkDetector {
	
	@Override
	protected AbstractLangElementHyperlink createHyperlink(IRegion requestedRegion, ITextEditor textEditor,
			IRegion wordRegion) {
		return new RustElementHyperlink(wordRegion, textEditor);
	}
	
	public class RustElementHyperlink extends AbstractLangElementHyperlink {
		
		public RustElementHyperlink(IRegion region, ITextEditor textEditor) {
			super(region, textEditor);
		}
		
		@Override
		public void open() {
			textEditor.doSave(new NullProgressMonitor());
			
			SourceRange sr = getElementRange();
			new RustOpenDefinitionOperation(textEditor, sr, OpenNewEditorMode.TRY_REUSING_EXISTING).
				executeAndHandle();
		}
		
	}
	
}