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

import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.toolchain.ops.IToolOperationService;
import melnorme.lang.tooling.toolchain.ops.SourceOpContext;
import melnorme.lang.utils.parse.StringCharSource;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.fields.validation.ValidatedValueSource;
import melnorme.utilbox.misc.Location;

public class RacerCompletionOperation extends RacerOperation<Indexable<ToolCompletionProposal>, LangCompletionResult> {
	
	protected final int offset;
	
	public RacerCompletionOperation(
			IToolOperationService opHelper, 
			ValidatedValueSource<Path> racerPath, ValidatedValueSource<Location> sdkSrcLocation, 
			SourceOpContext sourceOpContext) {
		super(opHelper, racerPath, sdkSrcLocation, sourceOpContext);
		this.offset = sourceOpContext.getOffset();
	}
	
	@Override
	protected Indexable<String> getRacerArguments() throws CommonException {
		int line_0 = sourceOpContext.getInvocationLine_0();
		int col_0 = sourceOpContext.getInvocationColumn_0();
		Location fileLocation = sourceOpContext.getFileLocation();
		return getArguments("complete-with-snippet", line_0, col_0, fileLocation);
	}
	
	@Override
	public LangCompletionResult parseProcessOutput(StringCharSource outputParseSource) 
			throws CommonException {
		return createRacerOutputParser(offset).parse(outputParseSource);
	}
	
	@Override
	protected LangCompletionResult createErrorResponse(String errorMessage) {
		return new LangCompletionResult(errorMessage);
	}
	
}