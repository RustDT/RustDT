/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.ide.ui.editor.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditor;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.ui.LangUIPlugin_Actual;
import melnorme.lang.ide.ui.editor.AbstractLangEditor;
import melnorme.lang.ide.ui.editor.LangEditorMessages;
import melnorme.lang.ide.ui.editor.structure.EditorStructureUtil;
import melnorme.lang.ide.ui.utils.operations.BasicEditorOperation;
import melnorme.lang.ide.ui.views.EnhancedSelectionDialog;
import melnorme.utilbox.core.CommonException;

public class OpenTypeHandler extends AbstractEditorHandler {
	public OpenTypeHandler(IWorkbenchPage page) {
		super(page);
	}
	
	@Override
	protected BasicEditorOperation createOperation(ITextEditor editor) {
		return new BasicEditorOperation(LangEditorMessages.QuickOutline_title, editor) {
			@Override
			protected void doRunWithEditor(AbstractLangEditor editor) throws CommonException {
				EnhancedSelectionDialog dialog = new EnhancedSelectionDialog(
					getShell(), LangUIPlugin_Actual.getStructureElementLabelProvider());
				
				dialog.setTitle("Open Type");
				dialog.setHelpAvailable(false);
				dialog.setMatchEmptyString(false);
				dialog.setMessage("Enter type name prefix or pattern (*, ? or camel case):");
				dialog.setElements(LangCore.getIndexManager().getGlobalSourceStructure().toArray());
				dialog.open();
				try {
					EditorStructureUtil.openInEditorAndReveal(dialog.getFirstResult());
				} catch(CoreException e) {
					throw new CommonException("Could not open global source structure", e);
				}
			}
		};
	}
	
}