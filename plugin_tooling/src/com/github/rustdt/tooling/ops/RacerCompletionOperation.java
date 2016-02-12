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
package com.github.rustdt.tooling.ops;

import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.ops.IOperationService;
import melnorme.utilbox.concurrency.ICancelMonitor;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RacerCompletionOperation extends RacerOperation {
	
	protected final int offset;
	
	public RacerCompletionOperation(IOperationService opHelper, String racerPath, String rustSrcPath, int offset,
			int line_0, int col_0, Location fileLocation) {
		super(opHelper, racerPath, rustSrcPath, getArguments("complete-with-snippet", line_0, col_0, fileLocation));
		
		this.offset = offset;
	}
	
	public LangCompletionResult executeAndProcessOutput(ICancelMonitor cm) 
			throws CommonException, OperationCancellation {
		ExternalProcessResult result = execute(cm);
		
		return createRacerOutputParser(offset).parse(result);
	}
	
}