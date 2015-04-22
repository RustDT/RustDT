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

import static melnorme.lang.tooling.CompletionProposalKind.Function;
import static melnorme.lang.tooling.CompletionProposalKind.Let;
import static melnorme.lang.tooling.CompletionProposalKind.Struct;
import static melnorme.lang.tooling.CompletionProposalKind.Trait;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.CoreUtil.listFrom;

import java.util.List;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.ToolingMessages;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.lang.tooling.ops.FindDefinitionResult;
import melnorme.lang.tooling.ops.OperationSoftFailure;
import melnorme.lang.tooling.ops.SourceLineColumnRange;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;

import org.junit.Test;


public class RacerOutputParserTest extends CommonToolingTest {
	
	protected RacerCompletionOutputParser createTestsParser(int offset) {
		return new RacerCompletionOutputParser(offset) {
			@Override
			protected void handleLineParseError(CommonException ce) {
				assertFail();
			}
			@Override
			protected void handleInvalidMatchKindString(String matchKindString) throws CommonException {
			}
		};
	}
	
	protected SourceRange sr(int offset, int length) {
		return new SourceRange(offset, length);
	}
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		int offset = 10;
		
		RacerCompletionOutputParser buildParser = createTestsParser(offset);
		
		testParseOutput(buildParser, "", listFrom());  // Empty
		
		testParseOutput(buildParser, 
			"PREFIX 1,1,\n" +
			"MATCH BufReader;BufReader;32;11;/RustProject/src/xpto.rs;Struct;pub struct BufReader<R> {\n" +
			"MATCH BufRead;BufRead;519;10;/RustProject/src/xpto2.rs;Trait;pub trait BufRead : Read {\n"
			, 
			listFrom(
				new ToolCompletionProposal(offset, 0, "BufReader", "BufReader", Struct, "xpto.rs"),
				new ToolCompletionProposal(offset, 0, "BufRead", "BufRead", Trait, "xpto2.rs")
			)
		);
		
		testParseOutput(buildParser, 
			"PREFIX 4,6,pr\n" +
			"MATCH BufReader;BufReader;32;11;relativeDir/src/xpto.rs;Let;pub struct BufReader<R> {\n"
			, 
			listFrom(
				new ToolCompletionProposal(offset-2, 2, "BufReader", "BufReader", Let, "xpto.rs")
			)
		);
		
		testParseOutput(buildParser, 
			"PREFIX 1,1,\n" +
			"MATCH stdin;stdin();122;7;/rustc-nightly/src/libstd/io/stdio.rs;Function;pub fn stdin() -> Stdin {\n" +
			"MATCH repeat;repeat(${1:byte: u8});75;7;/rustc-nightly/src/libstd/io/util.rs;Function;"
					+"pub fn repeat(byte: u8) -> Repeat { Repeat { byte: byte } }\n" +
			"MATCH copy;copy(${1:r: &mut R}, ${2:w: &mut W});31;7;/rustc-nightly/src/libstd/io/util.rs;Function;"
					+"pub fn copy<R: Read, W: Write>(r: &mut R, w: &mut W) -> io::Result<u64> {\n"
			, 
			listFrom(
				new ToolCompletionProposal(offset, 0, "stdin", "stdin()", Function, "stdio.rs",
					"stdin()", new ArrayList2<SourceRange>()),
				new ToolCompletionProposal(offset, 0, "repeat", "repeat(byte: u8)", Function, "util.rs",
					"repeat(byte)", new ArrayList2<>(sr(7, 4))),
				new ToolCompletionProposal(offset, 0, "copy", "copy(r: &mut R, w: &mut W)", Function, "util.rs",
					"copy(r, w)", new ArrayList2<>(sr(5, 1), sr(8, 1)))
			)
		);
		
		// Test error case
		testParseOutput(buildParser, 
			"PREFIX 1,1,\n" +
			"MATCH xxx;xxx(;122;7;/rustc-nightly/src/libstd/io/stdio.rs;Function;pub fn stdin() -> Stdin {\n"+
			"MATCH xxx2;xxx2(${;122;7;/rustc-nightly/src/libstd/io/stdio.rs;Function;pub fn stdin() -> Stdin {\n"
			, 
			listFrom(
				new ToolCompletionProposal(offset, 0, "xxx", "xxx()", Function, "stdio.rs",
					"xxx()", new ArrayList2<SourceRange>()),
				new ToolCompletionProposal(offset, 0, "xxx2", "xxx2()", Function, "stdio.rs",
					"xxx2(__)", new ArrayList2<SourceRange>(sr(5, 2)))
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
		
		RacerCompletionOutputParser buildParser = createTestsParser(0);
		
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