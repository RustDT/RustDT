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

import melnorme.lang.tooling.toolchain.ops.IToolOperationService;
import melnorme.lang.tooling.toolchain.ops.SourceLocation;
import melnorme.lang.tooling.toolchain.ops.SourceOpContext;
import melnorme.lang.tooling.toolchain.ops.ToolResponse;
import melnorme.lang.utils.parse.StringCharSource;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.fields.validation.ValidatedValueSource;
import melnorme.utilbox.misc.Location;

public class RacerFindDefinitionOperation extends RacerOperation<SourceLocation, ToolResponse<SourceLocation>> {
	
	protected final int offset;
	
	public RacerFindDefinitionOperation(IToolOperationService toolRunner, 
			ValidatedValueSource<Path> racerPath, ValidatedValueSource<Location> sdkSrcLocation, 
			SourceOpContext sourceOpContext) throws CommonException {
		super(toolRunner, racerPath, sdkSrcLocation, sourceOpContext);
		
		this.offset = sourceOpContext.getOffset();
	}
	
	@Override
	protected Indexable<String> getRacerArguments() throws CommonException {
		int line_0 = sourceOpContext.getInvocationLine_0();
		int col_0 = sourceOpContext.getInvocationColumn_0();
		return getArguments("find-definition", line_0, col_0, sourceOpContext.getFileLocation());
	}
	
	@Override
	public ToolResponse<SourceLocation> parseProcessOutput(StringCharSource output)
			throws CommonException {
		SourceLocation findDefResult = createRacerOutputParser(offset).parseResolvedMatch(output.getSource());
		return new ToolResponse<>(findDefResult);
	}
	
	@Override
	protected ToolResponse<SourceLocation> createErrorResponse(String errorMessage) {
		return createErrorToolResponse(errorMessage);
	}
	
}