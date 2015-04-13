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

import melnorme.lang.tooling.completion.LangCompletionProposal;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.utils.ParseHelper;
import melnorme.lang.utils.SimpleLexingHelper;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RacerOutputParser extends ParseHelper {
	
	protected final int offset;
	
	protected int prefixLength;

	public RacerOutputParser(int offset) {
		this.offset = offset;
	}
	
	public LangCompletionResult parse(ExternalProcessResult result) throws CommonException {
		if(result.exitValue != 0) {
			throw new CommonException("Tool exited with non-zero status: " + result.exitValue);
		}
		
		return parse(result.getStdOutBytes().toString());
	}
	
	protected LangCompletionResult parse(String input) throws CommonException {
		
		SimpleLexingHelper lexer = new SimpleLexingHelper(input);
		
		ArrayList2<LangCompletionProposal> proposals = new ArrayList2<>();
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
	
	protected void handleLineParseError(CommonException ce) throws CommonException {
		 throw ce;
	}
	
	protected void parsePrefix(String line) throws CommonException {
		SimpleLexingHelper lineLexer = new SimpleLexingHelper(line);
		lineLexer.tryConsume("PREFIX ");
		int prefixStart = parsePositiveInt(lineLexer.consumeDelimitedString(',', '\\'));
		int prefixEnd = parsePositiveInt(lineLexer.consumeDelimitedString(',', '\\'));
		
		prefixLength = prefixEnd - prefixStart;
	}
	
	protected LangCompletionProposal parseProposal(String line) {
		SimpleLexingHelper lineLexer = new SimpleLexingHelper(line);
		
		lineLexer.tryConsume("MATCH ");
		
		String replaceString = lineLexer.consumeDelimitedString(';', '\\');
		
		return new LangCompletionProposal(offset - prefixLength, replaceString, prefixLength);
	}
	
}