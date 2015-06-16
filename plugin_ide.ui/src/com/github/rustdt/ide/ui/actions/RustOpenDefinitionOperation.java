/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.actions;

import melnorme.lang.ide.core.operations.DaemonEnginePreferences;
import melnorme.lang.ide.ui.LangUIMessages;
import melnorme.lang.ide.ui.editor.EditorUtils.OpenNewEditorMode;
import melnorme.lang.ide.ui.editor.actions.AbstractOpenElementOperation;
import melnorme.lang.ide.ui.tools.ToolManagerOperationHelper;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.ops.FindDefinitionResult;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RacerFindDefinitionOperation;

public class RustOpenDefinitionOperation extends AbstractOpenElementOperation {
	
	public RustOpenDefinitionOperation(ITextEditor editor, SourceRange range, 
			OpenNewEditorMode openEditorMode) {
		super(LangUIMessages.Op_OpenDefinition_Name, editor, range, openEditorMode);
	}
	
	@Override
	protected void prepareOperation() throws CoreException, CommonException {
		super.prepareOperation();
		
		getContext().getEditor_nonNull().doSave(new NullProgressMonitor());
	}
	
	@Override
	protected FindDefinitionResult performLongRunningComputation_doAndGetResult(IProgressMonitor monitor)
			throws CoreException, OperationCancellation, CommonException {
		
		String racerPath = DaemonEnginePreferences.DAEMON_PATH.get();
		String sdkSrcPath = RustSDKPreferences.SDK_SRC_PATH.get();
		
		RacerFindDefinitionOperation op = new RacerFindDefinitionOperation(new ToolManagerOperationHelper(monitor), 
			racerPath, sdkSrcPath, range.getOffset(), line_0, col_0, inputLoc);
		return op.executeAndProcessOutput();
	}
	
}