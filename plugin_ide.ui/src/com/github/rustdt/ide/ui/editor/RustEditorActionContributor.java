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

import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.ide.ui.actions.RustOpenDefinitionOperation;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.utils.operation.EclipseCancelMonitor;
import melnorme.lang.ide.ui.editor.EditorUtils;
import melnorme.lang.ide.ui.editor.EditorUtils.OpenNewEditorMode;
import melnorme.lang.ide.ui.editor.LangEditorActionContributor;
import melnorme.lang.ide.ui.utils.operations.AbstractEditorOperation2;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;

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
	
	public static class RustFmtOperation extends AbstractEditorOperation2<Void> {
		
		public RustFmtOperation(ITextEditor editor) {
			super("Format", editor);
		}
		
		@Override
		protected Void doBackgroundValueComputation(IProgressMonitor monitor)
				throws CoreException, CommonException, OperationCancellation {
			Path rustFmt = RustSDKPreferences.RUSTFMT_PATH.getDerivedValue();
			
			ArrayList2<String> cmdLine = new ArrayList2<>(rustFmt.toString());
			ProcessBuilder pb = new ProcessBuilder(cmdLine);
			//"--skip-children"
			cmdLine.add("--write-mode=diff");
			cmdLine.add(this.inputLoc.toPathString());
			
			String docText = EditorUtils.getEditorDocument(editor).get();
			LangCore.getToolManager().runEngineTool(pb, docText, new EclipseCancelMonitor(monitor));
			
			return null;
		}
		
	}
	
}