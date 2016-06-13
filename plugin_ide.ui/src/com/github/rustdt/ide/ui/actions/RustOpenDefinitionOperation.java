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
package com.github.rustdt.ide.ui.actions;

import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RacerFindDefinitionOperation;

import melnorme.lang.ide.core.operations.ToolManager.ToolManagerEngineToolRunner;
import melnorme.lang.ide.ui.LangUIMessages;
import melnorme.lang.ide.ui.editor.EditorUtils.OpenNewEditorMode;
import melnorme.lang.ide.ui.editor.actions.AbstractOpenElementOperation;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.toolchain.ops.FindDefinitionResult;
import melnorme.lang.tooling.toolchain.ops.ToolResponse;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.status.StatusException;

public class RustOpenDefinitionOperation extends AbstractOpenElementOperation {
	
	public RustOpenDefinitionOperation(ITextEditor editor, SourceRange range, OpenNewEditorMode openEditorMode) {
		super(LangUIMessages.Op_OpenDefinition_Name, editor, range, openEditorMode);
	}
	
	@Override
	protected void prepareOperation() throws CommonException {
		super.prepareOperation();
	}
	
	@Override
	protected FindDefinitionResult performLongRunningComputation_doAndGetResult(IOperationMonitor monitor)
			throws OperationCancellation, CommonException {
		
		ToolManagerEngineToolRunner toolRunner = getToolManager().new ToolManagerEngineToolRunner();
		
		int line_0 = sourceOpContext.getInvocationLine_0();
		int col_0 = sourceOpContext.getInvocationColumn_0();
		
		RacerFindDefinitionOperation op = new RacerFindDefinitionOperation(toolRunner, 
			RustSDKPreferences.RACER_PATH.getValidatableValue(project) , 
			RustSDKPreferences.SDK_SRC_PATH3.getValidatableValue(project), 
			getSource(),
			sourceOpContext.isDocumentDirty(),
			getInvocationOffset(), line_0, col_0, getInputLocation());
		
		ToolResponse<FindDefinitionResult> opResult = op.execute(monitor);
		try {
			return opResult.getValidResult();
		} catch(StatusException e) {
			statusErrorMessage = e.getMessage();
			return null;
		}
		
	}
	
}