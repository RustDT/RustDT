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

import java.io.File;

import melnorme.lang.tooling.CompletionProposalKind;
import melnorme.lang.tooling.ElementAttributes;
import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.ToolingMessages;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.ops.AbstractToolOutputParser;
import melnorme.lang.tooling.ops.FindDefinitionResult;
import melnorme.lang.tooling.ops.SourceLineColumnRange;
import melnorme.lang.utils.parse.LexingUtils;
import melnorme.lang.utils.parse.StringParseSource;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;

public abstract class RacerCompletionOutputParser extends AbstractToolOutputParser<LangCompletionResult> {
	
	protected final int offset;
	
	protected int prefixLength;

	public RacerCompletionOutputParser(int offset) {
		this.offset = offset;
	}
	
	@Override
	protected String getToolProcessName() {
		return "Racer";
	}
	
	@Override
	protected LangCompletionResult parse(StringParseSource source) throws CommonException {
		
		ArrayList2<ToolCompletionProposal> proposals = new ArrayList2<>();
		prefixLength = 0;

		while(true) {
			String line = LexingUtils.consumeUntilDelimiter(source, '\n', '\\');
			
			if(line == null || line.isEmpty()) {
				return new LangCompletionResult(proposals);
			}
			
			if(line.startsWith("PREFIX ")) {
				parsePrefix(line);
			} else if(line.startsWith("MATCH ")) {
				proposals.add(parseProposal(line, source));
			} else if(line.startsWith("END")) {
                continue;
            } else {
				handleMessageParseError(new CommonException("Unknown line format: " + line));
			}
			
		}
		
	}
	
	protected void parsePrefix(String line) throws CommonException {
		StringParseSource lineLexer = new StringParseSource(line);
		lineLexer.tryConsume("PREFIX ");
		int prefixStart = parsePositiveInt(consumeCommaDelimitedString(lineLexer));
		int prefixEnd = parsePositiveInt(consumeCommaDelimitedString(lineLexer));
		
		prefixLength = prefixEnd - prefixStart;
	}
	
	protected String consumeCommaDelimitedString(StringParseSource lineLexer) {
		return LexingUtils.consumeUntilDelimiter(lineLexer, ',');
	}
	
	@SuppressWarnings("unused")
	public ToolCompletionProposal parseProposal(String line, StringParseSource source) throws CommonException {
		StringParseSource lineLexer = new StringParseSource(line);
		
		lineLexer.tryConsume("MATCH ");
		
		String baseName = consumeSemicolonDelimitedString(lineLexer);
		String rawLabel = consumeSemicolonDelimitedString(lineLexer);
		
		consumeSemicolonDelimitedString(lineLexer); // Line no
		consumeSemicolonDelimitedString(lineLexer); // Char no
		
		String rawModuleName = consumeSemicolonDelimitedString(lineLexer);
		String rawKind = consumeSemicolonDelimitedString(lineLexer);
		
		String description = consumeSemicolonDelimitedString(lineLexer);
		
		// Commented out - This wasn't really correct parsing
//		while(source.hasCharAhead() && !source.lookaheadMatches("MATCH ")) {
//			// This is a multiline description.
//			description += "\n" + assertNotNull(source.consumeLine());
//		}
		
		
		CompletionProposalKind kind = parseKind(rawKind); 
		String moduleName = parseModuleName(rawModuleName);
		ArrayList2<String> params = parseParametersFromRawLabel(rawLabel);
		
		String label = baseName;
		ArrayList2<SourceRange> subElements = null;
		String fullReplaceString = baseName;
		
		if(params != null) {
			label = baseName + "(" + StringUtil.collToString(params, ", ") + ")";
			subElements = new ArrayList2<>();
			fullReplaceString = buildFullReplacementStringAndSubElements(baseName, params, subElements);
		}
		
		if(kind == CompletionProposalKind.Module) {
			fullReplaceString = baseName + "::";
		}
		
		ElementAttributes attributes = new ElementAttributes(null); // TODO
		
		int completionOffset = offset - prefixLength;
		return new ToolCompletionProposal(
			completionOffset, prefixLength, baseName, 
			label, kind, attributes,
			moduleName, description,
			fullReplaceString, subElements);
	}
	
	protected String consumeSemicolonDelimitedString(StringParseSource lineLexer) {
		return LexingUtils.consumeUntilDelimiter(lineLexer, ';');
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
	
	/* -----------------  ----------------- */
	
	protected ArrayList2<String> parseParametersFromRawLabel(String rawLabel) {
		StringParseSource lexer = new StringParseSource(rawLabel);
		
		lexer.consumeUntil("(");
		if(!lexer.tryConsume("(")) {
			return null;
		}
		
		ArrayList2<String> args = new ArrayList2<>();
		
		while(!lexer.lookaheadIsEOF()){
			if(lexer.tryConsume(")")) {
				break;
			}
			if(lexer.tryConsume("${")) {
				args.add(parseRawLabelArg(lexer));
				continue;
			}
			lexer.consume();
		}
		
		return args;
	}
	
	protected String parseRawLabelArg(StringParseSource lexer) {
		lexer.consumeUntil(":", true);
		return lexer.consumeUntil("}", true);
	}
	
	protected String buildFullReplacementStringAndSubElements(String baseName, ArrayList2<String> params, 
			ArrayList2<SourceRange> subElements) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(baseName);
		
		sb.append("(");
		boolean first = true;
		for (String param : params) {
			if(!first) {
				sb.append(", ");
			}
			
			String paramName = getParamNameSuggestion(param);
			
			subElements.add(new SourceRange(sb.length(), paramName.length()));
			sb.append(paramName);
			
			first = false;
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	protected String getParamNameSuggestion(String param) {
		String declaredParamName = StringUtil.substringUntilMatch(param, ":");
		if(declaredParamName.isEmpty()) {
			return "__";
		}
		return declaredParamName;
	}
	
	/* ----------------- find-definition ----------------- */
	
	public FindDefinitionResult parseResolvedMatch(String lineInput) throws CommonException {
		return doParseResolvedMatch(lineInput);
	}
	
	protected FindDefinitionResult doParseResolvedMatch(String lineInput) throws CommonException {
		StringParseSource lineLexer = new StringParseSource(lineInput);
		
		if(lineLexer.tryConsume("MATCH ") == false) {
			throw new CommonException(ToolingMessages.FIND_DEFINITION_NoTargetFound);
		}
		
		@SuppressWarnings("unused")
		String elementName = consumeCommaDelimitedString(lineLexer);
		int line_1 = parsePositiveInt(consumeCommaDelimitedString(lineLexer));
		int column_0 = parsePositiveInt(consumeCommaDelimitedString(lineLexer));
		Location loc = parseLocation(consumeCommaDelimitedString(lineLexer));
		
		SourceLineColumnRange position = new SourceLineColumnRange(line_1, column_0 + 1);
		return new FindDefinitionResult(loc, position, null);
	}
	
}
