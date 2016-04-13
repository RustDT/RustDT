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

import java.nio.file.Path;

import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.data.IValidatableValue;
import melnorme.lang.tooling.ops.IToolOperationService;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;

public class RacerCompletionOperation extends RacerOperation<LangCompletionResult> {
	
	protected final int offset;
	
	public RacerCompletionOperation(IToolOperationService opHelper, 
			IValidatableValue<Path> racerPath, IValidatableValue<Location> sdkSrcLocation, 
			int offset, int line_0, int col_0, Location fileLocation) {
		super(opHelper, racerPath, sdkSrcLocation, getArguments("complete-with-snippet", line_0, col_0, fileLocation));
		
		this.offset = offset;
	}
	
	@Override
	protected LangCompletionResult handleProcessOutput(String output) throws CommonException {
		return createRacerOutputParser(offset).parse(output);
	}
	
}