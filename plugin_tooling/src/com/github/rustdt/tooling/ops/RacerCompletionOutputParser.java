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

import java.nio.file.Path;

import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.ToolingMessages;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.ops.AbstractToolOutputParser;
import melnorme.lang.tooling.ops.FindDefinitionResult;
import melnorme.lang.tooling.ops.SourceLineColumnRange;
import melnorme.lang.utils.SimpleLexingHelper;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;

public class RacerCompletionOutputParser extends AbstractToolOutputParser<LangCompletionResult> {
	
	protected final int offset;
	
	protected int prefixLength;

	public RacerCompletionOutputParser(int offset) {
		this.offset = offset;
	}
	
	@Override
	protected LangCompletionResult parse(String input) throws CommonException {
		
		SimpleLexingHelper lexer = new SimpleLexingHelper(input);
		
		ArrayList2<ToolCompletionProposal> proposals = new ArrayList2<>();
		prefixLength = 0;

		while(true) {
			String line = lexer.consumeDelimitedString('\n', '\\');
			
			if(line == null || line.isEmpty()) {
				return new LangCompletionResult(proposals);
			}
			
			if(line.startsWith("PREFIX ")) {
				parsePrefix(line);
			} else if(line.startsWith("MATCH ")) {
				proposals.add(parseProposal(line));
			} else {
				handleLineParseError(new CommonException("Unknown line format: " + line));
			}
			
		}
		
	}
	
	protected void parsePrefix(String line) throws CommonException {
		SimpleLexingHelper lineLexer = new SimpleLexingHelper(line);
		lineLexer.tryConsume("PREFIX ");
		int prefixStart = parsePositiveInt(lineLexer.consumeDelimitedString(',', '\\'));
		int prefixEnd = parsePositiveInt(lineLexer.consumeDelimitedString(',', '\\'));
		
		prefixLength = prefixEnd - prefixStart;
	}
	
	public ToolCompletionProposal parseProposal(String line) {
		SimpleLexingHelper lineLexer = new SimpleLexingHelper(line);
		
		lineLexer.tryConsume("MATCH ");
		
		String replaceString = lineLexer.consumeDelimitedString(';', '\\');
		
		return new ToolCompletionProposal(offset - prefixLength, replaceString, prefixLength);
	}
	
	public FindDefinitionResult parseResolvedMatch(String lineInput) {
		
		try {
			return doParseResolvedMatch(lineInput);
		} catch (CommonException e) {
			return new FindDefinitionResult(
				ToolingMessages.FIND_DEFINITION_ToolError + e.getMessage());
		}
	}
	
	protected FindDefinitionResult doParseResolvedMatch(String lineInput) throws CommonException {
		SimpleLexingHelper lineLexer = new SimpleLexingHelper(lineInput);
		
		if(lineLexer.tryConsume("MATCH ") == false) {
			return new FindDefinitionResult(ToolingMessages.FIND_DEFINITION_NoTargetFound);
		}
		
		@SuppressWarnings("unused")
		String elementName = lineLexer.consumeDelimitedString(',', '\\');
		int line_1 = parsePositiveInt(lineLexer.consumeDelimitedString(',', '\\'));
		int column_0 = parsePositiveInt(lineLexer.consumeDelimitedString(',', '\\'));
		Path path = parsePath(lineLexer.consumeDelimitedString(',', '\\'));
		
		SourceLineColumnRange position = new SourceLineColumnRange(path, line_1, column_0 + 1);
		return new FindDefinitionResult(null, position);
	}
	
}