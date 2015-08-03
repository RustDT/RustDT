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

import melnorme.lang.tooling.ops.FindDefinitionResult;
import melnorme.lang.tooling.ops.IOperationHelper;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RacerFindDefinitionOperation extends RacerOperation {
	
	protected final int offset;
	
	public RacerFindDefinitionOperation(IOperationHelper toolRunner, String racerPath, String sdkSrcPath, int offset,
			int line_0, int col_0, Location fileLocation) {
		super(toolRunner, racerPath, sdkSrcPath, getArguments("find-definition", line_0, col_0, fileLocation));
		
		this.offset = offset;
	}
	
	public FindDefinitionResult executeAndProcessOutput() throws CommonException, OperationCancellation {
		ProcessBuilder pb = getCommandProcessBuilder();
		ExternalProcessResult result = runToolProcess2(pb, input);
		
		String output = result.getStdOutBytes().toString(StringUtil.UTF8);
		return createRacerOutputParser(offset).parseResolvedMatch(output);
	}
	
}