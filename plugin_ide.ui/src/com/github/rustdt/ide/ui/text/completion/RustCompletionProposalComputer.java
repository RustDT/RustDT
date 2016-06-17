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
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.ide.ui.templates.LangTemplateProposal;
import melnorme.lang.ide.ui.text.completion.LangCompletionProposal;
import melnorme.lang.ide.ui.text.completion.LangCompletionProposalComputer;
import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.common.ops.IOperationMonitor.NullOperationMonitor;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.tooling.toolchain.ops.SourceOpContext;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.ICancelMonitor;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;

public class RustCompletionProposalComputer extends LangCompletionProposalComputer {
	
	@Override
	protected Indexable<ToolCompletionProposal> doComputeProposals(SourceOpContext sourceOpContext, ICancelMonitor cm) 
			throws CommonException, OperationCancellation, OperationSoftFailure {
		
		IProject project = ResourceUtils.getProject(sourceOpContext.getOptionalFileLocation());
		
		ToolManagerEngineToolRunner toolRunner = getEngineToolRunner();
		
		RacerCompletionOperation racerCompletionOp = new RacerCompletionOperation(toolRunner, 
			RustSDKPreferences.RACER_PATH.getValidatableValue(project),
			RustSDKPreferences.SDK_SRC_PATH3.getValidatableValue(project),
			sourceOpContext);
		
		return racerCompletionOp.executeToolOperation(new NullOperationMonitor(cm));
	}
	
	@Override
	protected ICompletionProposal adaptToolProposal(SourceOpContext sourceOpContext, ToolCompletionProposal proposal) {
		IContextInformation ctxInfo = null; // TODO: context information
		
		return new LangCompletionProposal(sourceOpContext, proposal, getImage(proposal), ctxInfo) {
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