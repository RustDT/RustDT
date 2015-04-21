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

import java.io.File;
import java.nio.file.Path;

import melnorme.lang.tooling.CompletionProposalKind;
import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.ToolingMessages;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.ops.AbstractToolOutputParser;
import melnorme.lang.tooling.ops.FindDefinitionResult;
import melnorme.lang.tooling.ops.SourceLineColumnRange;
import melnorme.lang.utils.SimpleLexingHelper;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.StringUtil;

public abstract class RacerCompletionOutputParser extends AbstractToolOutputParser<LangCompletionResult> {
	
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
		int prefixStart = parsePositiveInt(consumeCommaDelimitedString(lineLexer));
		int prefixEnd = parsePositiveInt(consumeCommaDelimitedString(lineLexer));
		
		prefixLength = prefixEnd - prefixStart;
	}
	
	protected String consumeCommaDelimitedString(SimpleLexingHelper lineLexer) {
		return lineLexer.consumeDelimitedString(',', -1);
	}
	
	public ToolCompletionProposal parseProposal(String line) throws CommonException {
		SimpleLexingHelper lineLexer = new SimpleLexingHelper(line);
		
		lineLexer.tryConsume("MATCH ");
		
		String replaceString = consumeSemicolonDelimitedString(lineLexer);
		String rawLabel = consumeSemicolonDelimitedString(lineLexer);
		
		consumeSemicolonDelimitedString(lineLexer); // Line no
		consumeSemicolonDelimitedString(lineLexer); // Char no
		
		String rawModuleName = consumeSemicolonDelimitedString(lineLexer);
		String rawKind = consumeSemicolonDelimitedString(lineLexer);
		
		String label = replaceString; // TODO: parse rawLabel
		CompletionProposalKind kind = parseKind(rawKind); 
		String moduleName = parseModuleName(rawModuleName);
		
		int completionOffset = offset - prefixLength;
		return new ToolCompletionProposal(completionOffset, prefixLength, replaceString, label, kind, moduleName);
	}
	
	protected String consumeSemicolonDelimitedString(SimpleLexingHelper lineLexer) {
		return lineLexer.consumeDelimitedString(';', -1);
	}
	
	protected CompletionProposalKind parseKind(String rawKind) throws CommonException {
		String matchKindString = rawKind;
		try {
			return CompletionProposalKind.valueOf(matchKindString);
		} catch (IllegalArgumentException e) {
			handleInvalidMatchKindString(matchKindString);
			return CompletionProposalKind.UNKNOWN;
		}
	}
	
	protected abstract void handleInvalidMatchKindString(String matchKindString) throws CommonException;
	
	protected String parseModuleName(String rawModuleName) {
		String moduleName = StringUtil.substringAfterLastMatch(rawModuleName, File.separator);
		// For Window OSes, also trim module name using "/"
		moduleName = StringUtil.substringAfterLastMatch(moduleName, "/");
		return moduleName;
	}
	
	/* ----------------- find-definition ----------------- */
	
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
		String elementName = consumeCommaDelimitedString(lineLexer);
		int line_1 = parsePositiveInt(consumeCommaDelimitedString(lineLexer));
		int column_0 = parsePositiveInt(consumeCommaDelimitedString(lineLexer));
		Path path = parsePath(consumeCommaDelimitedString(lineLexer));
		
		SourceLineColumnRange position = new SourceLineColumnRange(path, line_1, column_0 + 1);
		return new FindDefinitionResult(null, position);
	}
	
}