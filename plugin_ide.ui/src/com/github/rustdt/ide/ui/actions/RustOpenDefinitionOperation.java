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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RacerFindDefinitionOperation;

import melnorme.lang.ide.core.operations.AbstractToolManager.ToolManagerEngineToolRunner;
import melnorme.lang.ide.core.operations.ToolchainPreferences;
import melnorme.lang.ide.ui.LangUIMessages;
import melnorme.lang.ide.ui.editor.EditorUtils.OpenNewEditorMode;
import melnorme.lang.ide.ui.editor.actions.AbstractOpenElementOperation;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.ops.FindDefinitionResult;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;

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
		
		String racerPath = ToolchainPreferences.DAEMON_PATH.get();
		String sdkSrcPath = RustSDKPreferences.SDK_SRC_PATH.get();
		
		ToolManagerEngineToolRunner toolRunner = getToolManager().new ToolManagerEngineToolRunner(monitor, true);
		
		RacerFindDefinitionOperation op = new RacerFindDefinitionOperation(toolRunner, 
			racerPath, sdkSrcPath, range.getOffset(), line_0, col_0, inputLoc);
		return op.executeAndProcessOutput();
	}
	
}