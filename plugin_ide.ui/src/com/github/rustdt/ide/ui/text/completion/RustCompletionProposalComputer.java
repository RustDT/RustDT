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
package com.github.rustdt.ide.ui.text.completion;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RacerCompletionOperation;

import melnorme.lang.ide.core.operations.ToolManager.ToolManagerEngineToolRunner;
import melnorme.lang.ide.core.text.TextSourceUtils;
import melnorme.lang.ide.ui.editor.actions.EditorOperationContext;
import melnorme.lang.ide.ui.templates.LangTemplateProposal;
import melnorme.lang.ide.ui.text.completion.LangCompletionProposal;
import melnorme.lang.ide.ui.text.completion.LangCompletionProposalComputer;
import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.utilbox.concurrency.ICancelMonitor;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;

public class RustCompletionProposalComputer extends LangCompletionProposalComputer {
	
	@Override
	protected LangCompletionResult doComputeProposals(EditorOperationContext context, ICancelMonitor cm) 
			throws CommonException, OperationCancellation {
		
		IProject project = context.getProject();
		
		int line_0 = context.getContext().getInvocationLine_0();
		int col_0 = context.getContext().getInvocationColumn_0();
		Location fileLocation = context.getEditorInputLocation();
		
		ToolManagerEngineToolRunner toolRunner = getEngineToolRunner();
		
		RacerCompletionOperation racerCompletionOp = new RacerCompletionOperation(toolRunner, 
			RustSDKPreferences.RACER_PATH.getValidatableValue(project),
			RustSDKPreferences.SDK_SRC_PATH3.getValidatableValue(project),
			context.getSource(),
			context.isSourceDocumentDirty(),
			context.getOffset(),
			line_0, col_0, fileLocation);
		
		return racerCompletionOp.execute(cm).get();
	}
	
	@Override
	protected ICompletionProposal adaptToolProposal(ToolCompletionProposal proposal) {
		IContextInformation ctxInfo = null; // TODO: context information
		
		return new LangCompletionProposal(proposal, getImage(proposal), ctxInfo) {
			@Override
			protected boolean isValidPrefix(String prefix) {
				String rplString = getBaseReplaceString();
				return TextSourceUtils.isPrefix(prefix, rplString, false);
			}
			
			@Override
			public IInformationControlCreator getInformationControlCreator() {
				return LangTemplateProposal.createTemplateInformationControlCreator();
			}
		};
	}
	
}