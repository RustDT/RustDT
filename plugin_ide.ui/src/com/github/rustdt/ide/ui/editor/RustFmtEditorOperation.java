/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
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

import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RustFmtOperation;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ToolManager;
import melnorme.lang.ide.ui.editor.actions.AbstractEditorToolOperation;
import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.toolchain.ops.ToolResponse;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;

public class RustFmtEditorOperation extends AbstractEditorToolOperation<String> {
	
	protected final ToolManager toolMgr = LangCore.getToolManager();
	
	public RustFmtEditorOperation(ITextEditor editor) {
		super("Format", editor);
	}
	
	@Override
	protected ToolResponse<String> doBackgroundValueComputation(IOperationMonitor monitor)
			throws CommonException, OperationCancellation {
		
		Path rustFmt = RustSDKPreferences.RUSTFMT_PATH.getDerivedValue(project);
		return new RustFmtOperation(getContext2(), getToolService(), rustFmt).executeOp(monitor);
	}
	
	@Override
	protected void handleResultData(String resultData) throws CommonException {
		if(resultData != null) {
			setEditorTextPreservingCarret(resultData);
		}
	}
	
}