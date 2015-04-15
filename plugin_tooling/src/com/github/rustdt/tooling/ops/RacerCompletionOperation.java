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
package com.github.rustdt.tooling.ops;

import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.ops.IProcessRunner;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RacerCompletionOperation extends RacerOperation {
	
	protected final int offset;
	
	public RacerCompletionOperation(IProcessRunner processRunner, String racerPath, String rustSrcPath, int offset,
			int line_0, int col_0, Location fileLocation) {
		super(processRunner, racerPath, rustSrcPath, getArguments("complete-with-snippet", line_0, col_0, fileLocation));
		
		this.offset = offset;
	}
	
	public LangCompletionResult executeAndProcessOutput() throws CommonException, OperationCancellation {
		ExternalProcessResult result = execute();
		
		return new RacerCompletionOutputParser(offset).parse(result);
	}
	
}