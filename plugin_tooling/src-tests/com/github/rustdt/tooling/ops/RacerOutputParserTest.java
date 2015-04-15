/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling.ops;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.CoreUtil.listFrom;

import java.util.List;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.ToolingMessages;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.ops.FindDefinitionResult;
import melnorme.lang.tooling.ops.OperationSoftFailure;
import melnorme.lang.tooling.ops.SourceLineColumnRange;
import melnorme.utilbox.core.CommonException;

import org.junit.Test;


public class RacerOutputParserTest extends CommonToolingTest {
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		int offset = 10;
		
		RacerCompletionOutputParser buildParser = new RacerCompletionOutputParser(offset) {
			@Override
			protected void handleLineParseError(CommonException ce) {
				assertFail();
			}
		};
		
		testParseOutput(buildParser, "", listFrom());  // Empty
		
		testParseOutput(buildParser, 
			"PREFIX 1,1,\n" +
			"MATCH BufReader;BufReader;32;11;/RustProject/src/xpto.rs;Struct;pub struct BufReader<R> {\n" +
			"MATCH BufRead;BufRead;519;10;/RustProject/src/xpto2.rs;Trait;pub trait BufRead : Read {\n"
			, 
			listFrom(
				new ToolCompletionProposal(offset, "BufReader", 0),
				new ToolCompletionProposal(offset, "BufRead", 0)
			)
		);
		
		testParseOutput(buildParser, 
			"PREFIX 4,6,pr\n" +
			"MATCH BufReader;BufReader;32;11;/RustProject/src/xpto.rs;Struct;pub struct BufReader<R> {\n"
			, 
			listFrom(
				new ToolCompletionProposal(offset-2, "BufReader", 2)
			)
		);
	}
	
	protected void testParseOutput(RacerCompletionOutputParser parser, String output, List<?> expected) 
			throws CommonException, OperationSoftFailure {
		LangCompletionResult result = parser.parse(output);
		assertAreEqualLists((List<?>) result.getValidatedProposals(), expected);
	}
	
	@Test
	public void parseMatchLine() throws Exception { parseMatchLine$(); }
	public void parseMatchLine$() throws Exception {
		
		RacerCompletionOutputParser buildParser = new RacerCompletionOutputParser(0) {
			@Override
			protected void handleLineParseError(CommonException ce) {
				assertFail();
			}
		};
		
		testParseResolvedMatch(buildParser, "", 
			new FindDefinitionResult(ToolingMessages.FIND_DEFINITION_NoTargetFound));
		
		testParseResolvedMatch(buildParser, 
			"MATCH other,19,3,/devel/RustProj/src/main.rs,Function,fn other(foo: i32) {", 
			new FindDefinitionResult(null, new SourceLineColumnRange(path("/devel/RustProj/src/main.rs"), 19, 4)));
		
		testParseResolvedMatch(buildParser, "MATCH other,as", 
			new FindDefinitionResult("Error: Invalid integer: `as`"));
		
	}
	
	protected void testParseResolvedMatch(RacerCompletionOutputParser buildParser, String input,
			FindDefinitionResult expected) {
		FindDefinitionResult result = buildParser.parseResolvedMatch(input);
		assertAreEqual(result, expected);
	}
	
}