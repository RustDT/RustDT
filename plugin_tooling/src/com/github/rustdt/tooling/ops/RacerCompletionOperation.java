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
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RacerCompletionOperation extends RacerOperation {
	
	protected final int offset;
	
	public RacerCompletionOperation(IProcessRunner processRunner, String toolPath, String rustPath, int offset,
			int line_0, int col_0, Location fileLocation) {
		super(processRunner, toolPath, rustPath, getArguments(line_0, col_0, fileLocation));
		
		this.offset = offset;
	}
	
	protected static ArrayList2<String> getArguments(int line_0, int col_0, Location fileLocation) {
		ArrayList2<String> arguments = new ArrayList2<String>("complete-with-snippet");
		arguments.add(Integer.toString(line_0 + 1)); // use one-based index
		arguments.add(Integer.toString(col_0)); // But this one is zero-based index
		arguments.add(fileLocation.toPathString());
		return arguments;
	}
	
	public LangCompletionResult executeAndProcessOutput() throws CommonException, OperationCancellation {
		ExternalProcessResult result = execute();
		
		return new RacerOutputParser(offset).parse(result);
	}
	
}