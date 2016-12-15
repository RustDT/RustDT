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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RacerCompletionResult;
import com.github.rustdt.tooling.ops.RacerFindDefinitionOperation;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ToolManager.ToolManagerEngineToolRunner;
import melnorme.lang.ide.core.text.JavaWordFinder;
import melnorme.lang.ide.ui.LangUIMessages;
import melnorme.lang.ide.ui.dialogs.PickTypeDialog;
import melnorme.lang.ide.ui.editor.EditorUtils.OpenNewEditorMode;
import melnorme.lang.ide.ui.editor.actions.AbstractOpenElementOperation;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.tooling.toolchain.ops.SourceLocation;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;

public class RustOpenDefinitionOperation extends AbstractOpenElementOperation {
	
	public RustOpenDefinitionOperation(ITextEditor editor, SourceRange range, OpenNewEditorMode openEditorMode) {
		super(LangUIMessages.Op_OpenDefinition_Name, editor, range, openEditorMode);
	}
	
	@Override
	protected SourceLocation doBackgroundToolResultComputation(IOperationMonitor om)
		throws CommonException, OperationCancellation, OperationSoftFailure {
		try {
			try {
				return getLocationProvidedByRacer(om);
			} catch(CommonException e) {
				return getLocationProvidedByIndex(e);
			}
		} catch(BadLocationException e) {
			throw new CommonException(e.getMessage(), e);
		}
	}
	
	private SourceLocation getLocationProvidedByRacer(IOperationMonitor om)
		throws CommonException, OperationCancellation, OperationSoftFailure, BadLocationException {
		ToolManagerEngineToolRunner toolRunner = getToolManager().new ToolManagerEngineToolRunner();
		RacerFindDefinitionOperation op =
			new RacerFindDefinitionOperation(toolRunner, RustSDKPreferences.RACER_PATH.getValidatableValue(project),
				RustSDKPreferences.SDK_SRC_PATH3.getValidatableValue(project), getSourceOpContext());
		RacerCompletionResult racerCompletionResult = op.executeToolOperation(om);
		return SourceLocation.forOneBasedLineAndColNumber(racerCompletionResult.location,
			racerCompletionResult.oneBasedLineNumber, racerCompletionResult.zeroBasedColumnNumber);
	}
	
	private SourceLocation getLocationProvidedByIndex(CommonException initialException)
		throws BadLocationException, CommonException {
		String searchTerm = getSearchTerm();
		List<StructureElement> candidates = LangCore.getIndexManager().getGlobalSourceStructure().stream()
			.filter(structureElement -> structureElement.getName().equals(searchTerm))
			.collect(Collectors.toList());
		
		StructureElement selectedCandidate;
		if(candidates.isEmpty()) {
			throw initialException;
		} else if(candidates.size() == 1) {
			selectedCandidate = candidates.get(0);
		} else {
			Shell shell = editor.getSite().getWorkbenchWindow().getShell();
			AtomicReference<StructureElement> reference = new AtomicReference<>();
			Display.getDefault().syncExec(
				() -> reference.set(PickTypeDialog.show(shell, candidates, true).orElse(null)));
			selectedCandidate = reference.get();
		}
		
		return selectedCandidate != null ? SourceLocation.forSourceRange(selectedCandidate.getLocation().get(),
			selectedCandidate.getNameSourceRange2()) : null;
	}

	private String getSearchTerm() throws BadLocationException {
		boolean somethingWasSelected = range.length != 0;
		if(somethingWasSelected) {
			return doc.get(range.offset, range.length);
		} else {
			IRegion wordRegionAroundCursor = JavaWordFinder.findWord(doc, range.offset);
			return doc.get(wordRegionAroundCursor.getOffset(), wordRegionAroundCursor.getLength());
		}
	}
}