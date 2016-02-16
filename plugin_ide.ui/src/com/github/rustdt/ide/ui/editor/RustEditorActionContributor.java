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

import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.ui.actions.RustOpenDefinitionOperation;

import melnorme.lang.ide.ui.editor.EditorUtils.OpenNewEditorMode;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.ide.ui.editor.LangEditorActionContributor;

public class RustEditorActionContributor extends LangEditorActionContributor {
	
	@Override
	protected RustOpenDefinitionOperation createOpenDefinitionOperation(ITextEditor editor, SourceRange range,
			OpenNewEditorMode newEditorMode) {
		return new RustOpenDefinitionOperation(editor, range, newEditorMode);
	}
	
	@Override
	protected void registerOtherEditorHandlers() {
	}
	
	@Override
	protected IEditorOperationCreator getOpCreator_Format() {
		return editor -> new RustFmtOperation(editor);
	}
	
}