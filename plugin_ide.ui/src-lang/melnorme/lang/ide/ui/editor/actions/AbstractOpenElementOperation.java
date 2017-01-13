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
package melnorme.lang.ide.ui.editor.actions;


import static melnorme.utilbox.core.CoreUtil.areEqual;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ToolManager;
import melnorme.lang.ide.core.utils.EclipseUtils;
import melnorme.lang.ide.ui.EditorSettings_Actual;
import melnorme.lang.ide.ui.editor.EditorUtils;
import melnorme.lang.ide.ui.editor.EditorUtils.OpenNewEditorMode;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.tooling.toolchain.ops.SourceLocation;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;

public abstract class AbstractOpenElementOperation extends AbstractEditorToolOperation<SourceLocation> {
	
	protected final OpenNewEditorMode openEditorMode;
	
	public AbstractOpenElementOperation(String operationName, ITextEditor editor, SourceRange range, 
			OpenNewEditorMode openEditorMode) {
		super(operationName, editor, range);
		
		this.openEditorMode = openEditorMode;
	}
	
	public int getInvocationOffset() {
		return getOperationOffset();
	}
	
	protected ToolManager getToolManager() {
		return LangCore.getToolManager();
	}
	
	@Override
	protected abstract SourceLocation doBackgroundToolResultComputation(IOperationMonitor om)
			throws CommonException, OperationCancellation, OperationSoftFailure;
	
	@Override
	protected void handleResultData(SourceLocation sourceLocation) throws CommonException {
		EclipseUtils.run(() -> openEditorForLocation(sourceLocation.getFileLocation(), sourceLocation));
	}
	
	protected void openEditorForLocation(Location fileLoc, SourceLocation sourceLocation)
		throws CoreException, CommonException {
		IEditorInput newInput = getNewEditorInput(fileLoc);
		ITextEditor newEditor = EditorUtils.openTextEditorAndSetSelection(editor, EditorSettings_Actual.EDITOR_ID,
			newInput, openEditorMode, null);
		
		IDocument doc = EditorUtils.getEditorDocument(newEditor);
		SourceRange sourceRange = sourceLocation.getSourceRange((line, col) -> getOffsetFrom(doc, line, col));

		
		EditorUtils.setEditorSelection(newEditor, sourceRange);
	}
	
	protected static int getOffsetFrom(IDocument doc, int line_oneBased, int column_oneBased) {
		try {
			int lineOffset = doc.getLineOffset(line_oneBased - 1);
			return lineOffset + column_oneBased - 1;
		} catch(BadLocationException e) {
			LangCore.logError("Invalid line number: " + line_oneBased, e);
			return 0;
		}
	}
	
	protected IEditorInput getNewEditorInput(Location newEditorFilePath) throws CommonException {
		if(newEditorFilePath == null) {
			throw new CommonException("No path provided for new element. ");
		}
		
		if(areEqual(newEditorFilePath, getInputLocation())) {
			return editor.getEditorInput();
		} else {
			return EditorUtils.getBestEditorInputForLoc(newEditorFilePath);
		}
	}
}