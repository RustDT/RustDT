/*******************************************************************************
 * Copyright (c) 2014 Bruno Medeiros and other Contributors.
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
import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import static melnorme.utilbox.core.CoreUtil.listFrom;

import java.util.List;

import org.junit.Test;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tooling.ElementAttributes;
import melnorme.lang.tooling.ToolCompletionProposal;
import melnorme.lang.tooling.ToolingMessages;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.MiscUtil;


public class RacerOutputParserTest extends CommonToolingTest {
	
	protected static final String NL = "\n";
	
	protected static final String DESC2 = "pub trait BufRead : Read {";
	protected static final String DESC1 = "pub struct BufReader<R> {";
	
	protected RacerCompletionOutputParser createTestsParser(int offset) {
		return new RacerCompletionOutputParser(offset) {
			@Override
			protected void handleInvalidMatchKindString(String matchKindString) throws CommonException {
			}
		};
	}
	
	protected SourceRange sr(int offset, int length) {
		return new SourceRange(offset, length);
	}
	
	protected static ElementAttributes att() {
		return new ElementAttributes(null);
	}
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		int offset = 10;
		
		RacerCompletionOutputParser buildParser = createTestsParser(offset);
		
		testParseOutput(buildParser, "", listFrom());  // Empty
		
		testParseOutput(buildParser, 
			"PREFIX 1,1,\n" +
			"MATCH BufReader;BufReader;32;11;/RustProject/src/xpto.rs;Struct;"+DESC1+NL+
			"MATCH BufRead;BufRead;519;10;/RustProject/src/xpto2.rs;Trait;"+DESC2+NL
			, 
			listFrom(
				new ToolCompletionProposal(offset, 0, "BufReader", "BufReader", Struct, att(), null, "xpto.rs", DESC1),
				new ToolCompletionProposal(offset, 0, "BufRead", "BufRead", Trait, att(), null, "xpto2.rs", DESC2)
			)
		);
		
		testParseOutput(buildParser, 
			"PREFIX 4,6,pr\n" +
			"MATCH BufReader;BufReader;32;11;relativeDir/src/xpto.rs;Let;"+DESC1+NL+
			"END\n"
			, 
			listFrom(
				new ToolCompletionProposal(offset-2, 2, "BufReader", "BufReader", Let, att(), null, "xpto.rs", DESC1)
			)
		);
		
		testParseOutput(buildParser, 
			"PREFIX 1,1,\n" +
			"MATCH stdin;stdin();122;7;/rustc-nightly/src/libstd/io/stdio.rs;Function;pub fn stdin() -> Stdin {"+NL+
			"MATCH repeat;repeat(${1:byte: u8});75;7;/rustc-nightly/src/libstd/io/util.rs;Function;"
					+"pub fn repeat(byte: u8) -> Repeat { Repeat { byte: byte } }" +NL+
			"MATCH copy;copy(${1:r: &mut R}, ${2:w: &mut W});31;7;/rustc-nightly/src/libstd/io/util.rs;Function;"
					+"pub fn copy<R: Read, W: Write>(r: &mut R, w: &mut W) -> io::Result<u64> {" +NL
			, 
			listFrom(
				new ToolCompletionProposal(offset, 0, "stdin", "stdin()", Function, att(), null, "stdio.rs",
					"pub fn stdin() -> Stdin {",
					"stdin()", new ArrayList2<SourceRange>()),
				new ToolCompletionProposal(offset, 0, "repeat", "repeat(byte: u8)", Function, att(), null, "util.rs",
					"pub fn repeat(byte: u8) -> Repeat { Repeat { byte: byte } }",
					"repeat(byte)", ArrayList2.create(sr(7, 4))),
				new ToolCompletionProposal(offset, 0, "copy", "copy(r: &mut R, w: &mut W)", Function, att(), 
					null, "util.rs",
					"pub fn copy<R: Read, W: Write>(r: &mut R, w: &mut W) -> io::Result<u64> {",
					"copy(r, w)", ArrayList2.create(sr(5, 1), sr(8, 1)))
			)
		);
		
		// Test error case
		testParseOutput(buildParser, 
			"PREFIX 1,1,\n" +
			"MATCH xxx;xxx(;122;7;/rustc-nightly/src/libstd/io/stdio.rs;Function;pub fn stdin() -> Stdin {\n"+
			"MATCH xxx2;xxx2(${;122;7;/rustc-nightly/src/libstd/io/stdio.rs;Function;pub fn stdin() -> Stdin {\n"
			, 
			listFrom(
				new ToolCompletionProposal(offset, 0, "xxx", "xxx()", Function, att(), null, "stdio.rs",
					"pub fn stdin() -> Stdin {",
					"xxx()", new ArrayList2<SourceRange>()),
				new ToolCompletionProposal(offset, 0, "xxx2", "xxx2()", Function, att(), null, "stdio.rs",
					"pub fn stdin() -> Stdin {",
					"xxx2(__)", ArrayList2.create(sr(5, 2)))
			)
		);
		
		// Commented out, to be fully removed
//		testMultiLineDesc(offset, buildParser);
	}
	
	protected void testParseOutput(RacerCompletionOutputParser parser, String output, List<?> expected)  {
		try {
			ArrayList2<ToolCompletionProposal> proposals = parser.parseOutput(output);
			assertAreEqualLists(proposals, expected);
		} catch(CommonException | OperationSoftFailure e) {
			assertFail();
		}
	}
	
	protected static final String DESC_LINE2 = "          where K: AsRef<OsStr>, V: AsRef<OsStr>";
	
	protected void testMultiLineDesc(int offset, RacerCompletionOutputParser buildParser) 
			throws CommonException {
		// Test multi-line description
		testParseOutput(buildParser, 
			"PREFIX 1,1,\n" +
			"MATCH xxx;xxx(;122;7;/rustc-nightly/src/libstd/io/stdio.rs;Function;pub fn stdin() -> Stdin {"+NL+
			DESC_LINE2 +NL+
			"MATCH xxx2;xxx2(${;122;7;/rustc-nightly/src/libstd/io/stdio.rs;Function;pub fn xxx2() -> Stdin {"+NL+
			DESC_LINE2 +NL+
			DESC_LINE2
			, 
			listFrom(
				new ToolCompletionProposal(offset, 0, "xxx", "xxx()", Function, att(), null, "stdio.rs",
					"pub fn stdin() -> Stdin {" +NL+ DESC_LINE2,
					"xxx()", new ArrayList2<SourceRange>()),
				new ToolCompletionProposal(offset, 0, "xxx2", "xxx2()", Function, att(), null, "stdio.rs",
					"pub fn xxx2() -> Stdin {" +NL+ DESC_LINE2 +NL+ DESC_LINE2,
					"xxx2(__)", ArrayList2.create(sr(5, 2)))
			)
		);
	}
	
	@Test
	public void parseMatchLine() throws Exception { parseMatchLine$(); }
	public void parseMatchLine$() throws Exception {
		
		RacerCompletionOutputParser buildParser = createTestsParser(0);
		
		testParseResolvedMatch(buildParser, "", null, ToolingMessages.FIND_DEFINITION_NoTargetFound);
		
		String LOC_ROOT = MiscUtil.OS_IS_WINDOWS ? "C:\\" : "/";
		
		testParseResolvedMatch(buildParser, 
			"MATCH other,19,3,"+LOC_ROOT+"RustProj/src/main.rs,Function,fn other(foo: i32) {", 
			new RacerCompletionResult(loc(LOC_ROOT + "/RustProj/src/main.rs"), 19, 4),
			null);
		
		testParseResolvedMatch(buildParser, "MATCH other,as", null, "Invalid integer: `as`");
		
	}
	
	protected void testParseResolvedMatch(RacerCompletionOutputParser buildParser, String input,
		RacerCompletionResult expected, String expectedException) {
		try {
			RacerCompletionResult result = buildParser.parseResolvedMatch(input);
			assertEquals(result.location, expected.location);
			assertEquals(result.oneBasedLineNumber, expected.oneBasedLineNumber);
			assertEquals(result.zeroBasedColumnNumber, expected.zeroBasedColumnNumber);
			assertTrue(expectedException == null);
		} catch(CommonException e) {
			assertNotNull(expectedException);
			assertTrue(e.getMessage().contains(expectedException));
		}
	}
	
}