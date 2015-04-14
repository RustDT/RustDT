/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.editor;

import melnorme.lang.ide.core.bundlemodel.SDKPreferences;
import melnorme.lang.ide.core.operations.DaemonEnginePreferences;
import melnorme.lang.ide.core.operations.TimeoutProgressMonitor;
import melnorme.lang.ide.ui.text.completion.LangCompletionProposalComputer;
import melnorme.lang.ide.ui.text.completion.LangContentAssistInvocationContext;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;

import org.eclipse.core.runtime.CoreException;

import com.github.rustdt.tooling.ops.RacerCompletionOperation;

public class RustCompletionProposalComputer extends LangCompletionProposalComputer {
	
	@Override
	protected LangCompletionResult doComputeProposals(LangContentAssistInvocationContext context, int offset,
			TimeoutProgressMonitor pm) throws CoreException, CommonException, OperationCancellation {
		String racerPath = DaemonEnginePreferences.DAEMON_PATH.get();
		String sdkSrcPath = SDKPreferences.SDK_PATH.get();
		
		int line_0 = context.getInvocationLine_0();
		int col_0 = context.getInvocationColumn_0();
		Location fileLocation = context.getEditorInputLocation();
		
		ArrayList2<String> arguments = new ArrayList2<String>("complete");
		arguments.add(Integer.toString(line_0 + 1)); // use one-based index
		arguments.add(Integer.toString(col_0 + 1)); // use one-based index
		arguments.add(fileLocation.toPathString());
		
		
		RacerCompletionOperation racerCompletionOp = new RacerCompletionOperation(new ToolProcessRunner(pm), 
			racerPath, sdkSrcPath, offset, arguments);
		return racerCompletionOp.executeAndProcessOutput();
	}
	
}