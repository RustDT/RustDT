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
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RacerCompletionOperation extends RacerOperation {
	
	protected final int offset;
	
	public RacerCompletionOperation(IProcessRunner processRunner, String toolPath, String rustPath, int offset,
			ArrayList2<String> arguments) {
		super(processRunner, toolPath, rustPath, arguments);
		this.offset = offset;
	}
	
	public LangCompletionResult executeAndProcessOutput() throws CommonException, OperationCancellation {
		ExternalProcessResult result = execute();
		
		return new RacerOutputParser(offset).parse(result);
	}
	
}