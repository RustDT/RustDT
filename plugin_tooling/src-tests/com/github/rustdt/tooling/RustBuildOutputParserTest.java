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
package com.github.rustdt.tooling;


import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.CoreUtil.listFrom;
import static melnorme.utilbox.status.Severity.ERROR;
import static melnorme.utilbox.status.Severity.WARNING;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tooling.common.SourceLineColumnRange;
import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.status.Severity;


public class RustBuildOutputParserTest extends CommonToolingTest {
	
	public class TestsRustBuildOutputParser extends RustBuildOutputParser {
		@Override
		protected void handleParseError(CommonException ce) {
			assertFail();
		}
	}
	
	protected static final String NL = "\n";
	
	protected static ToolSourceMessage msg(Path path, int line, int column, int endLine, int endColumn, 
			Severity severity, String errorMessage) {
		
		ToolSourceMessage msg = new ToolSourceMessage(path, 
			new SourceLineColumnRange(line, column, endLine, endColumn), 
			severity, errorMessage);
		msg.toString();
		return msg;
	}
	
	protected static String MULTILINE_MESSAGE = 
			"mismatched types:\n" +
			"		 expected `u32`,\n" +
			"		    found `core::option::Option<_>`";
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		RustBuildOutputParser buildParser = new TestsRustBuildOutputParser();
		RustBuildOutputParser buildParser_allowParseErrors = new RustBuildOutputParser() {
			@Override
			protected void handleParseError(CommonException ce) {
			}
		};
		
		testParseMessages(buildParser, "", listFrom());  // Empty
		
		
		{
			testParseMessages(buildParser_allowParseErrors, "libbar/blah.rs:", listFrom());
			testParseMessages(buildParser_allowParseErrors, "libbar/blah.rs:1:2: info: BLAH BLAH", listFrom());
			
			testParseMessages(buildParser_allowParseErrors, "libbar/blah.rs:1:2: 3:16 info: BLAH BLAH", listFrom());
		}
		
		testParseMessages(buildParser, 
			"src/main.rs:1:2: 3:17 warning: BLAH BLAH BLAH\n", 
			listFrom(msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH")));
		
		testParseMessages(buildParser, 
			"src/main.rs:1:2: warning: BLAH BLAH BLAH\n", 
			listFrom(msg(path("src/main.rs"), 1, 2, -1, -1, WARNING, "BLAH BLAH BLAH")));

		testParseMessages(buildParser, 
			"src/main.rs:1: warning: BLAH BLAH BLAH\n", 
			listFrom(msg(path("src/main.rs"), 1, -1, -1, -1, WARNING, "BLAH BLAH BLAH")));
		
		
		testParseMessages(buildParser_allowParseErrors, 
			"src/main.rs:1:2: 3:17 warning: BLAH BLAH BLAH\n" +
			"src/main.rs:1:2: 3: warning: INVALID\n" +
			"src/main.rs:1:2: :17 warning: INVALID\n" +
			"src/main.rs:1:2: 3:17 blah: INVALID\n" +
			"src/main.rs:1: warning: BLAH BLAH BLAH\n" +
			"src/foo/main.rs:2:1: 4:18 error: XXX\n",
			listFrom(
				msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH"),
				msg(path("src/main.rs"), 1, -1, -1, -1, WARNING, "BLAH BLAH BLAH"),
				msg(path("src/foo/main.rs"), 2, 1, 4, 18, ERROR, "XXX")
			)
		);
		
		// test Rust message source text component
		testParseMessages(buildParser, 
			"src/main.rs:1:2: 3:17 warning: BLAH BLAH BLAH\n"+
			"src/main.rs:1 const STRING2 : str  = xxx;" +NL+
			"                                     ^~~" +NL+
			"src/main.rs:2:2: 3:10 error: XXX\n"+
			"src/main.rs:1 const STRING2 : str  = ;" +NL+
			"                                     ^"
			, 
			listFrom(
				msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH"),
				msg(path("src/main.rs"), 2, 2, 3, 10, ERROR, "XXX")
			)
		);
		
		// Test actual multiline message
		testParseMessages(buildParser, 
			"src/main.rs:1:2: 3:17 error: " + MULTILINE_MESSAGE + NL +
			"src/main.rs:1: warning: xxx" + MULTILINE_MESSAGE,
			listFrom(
				msg(path("src/main.rs"), 1, 2, 3, 17, ERROR, MULTILINE_MESSAGE),
				msg(path("src/main.rs"), 1, -1, -1, -1, WARNING, "xxx" + MULTILINE_MESSAGE)
			)
		);
		
		// Test "aborting due to "
		testParseMessages(buildParser, 
			"src/main.rs:1:2: warning: BLAH BLAH BLAH"+NL+
			"error: aborting due to 3 previous errors", 
			listFrom(msg(path("src/main.rs"), 1, 2, -1, -1, WARNING, "BLAH BLAH BLAH")));
		
		testParseMessages(buildParser, 
			"error: aborting due to previous error", 
			listFrom());
		
		testMacroExpansionMessages(buildParser);
	}
	
	protected void testParseMessages(RustBuildOutputParser buildProcessor, String stderr, List<?> expected) 
			throws CommonException {
		ArrayList<ToolSourceMessage> buildMessages = buildProcessor.parseOutput(stderr);
		assertEquals(buildMessages, expected);
	}
	
	public static final String MACRO_MSG_1 = "src/main.rs:52:18: 52:28 error: unresolved name `abc`";
	public static final String MACRO_MSG_2 = "src/main.rs:46:1: 58:2 note: in expansion of create_function!";
	public static final String MACRO_MSG_3 = "src/main.rs:60:1: 60:23 note: expansion site";
	
	protected void testMacroExpansionMessages(RustBuildOutputParser buildParser) throws CommonException {
		// Test macro expansion error message
		testParseMessages(buildParser,
			MACRO_MSG_1 +NL+
			"src/main.rs:52         	let a = $func_name;" +NL+
			"                       	        ^~~~~~~~~~" +NL+
			MACRO_MSG_2 +NL+
			MACRO_MSG_3 +NL+
			"error: aborting due to previous error",
			
			listFrom(
				msg(path("src/main.rs"), 52, 18, 52, 28, ERROR, "unresolved name `abc`" 
						+NL+ MACRO_MSG_2 +NL+ MACRO_MSG_3)
				, msg(path("src/main.rs"), 60, 1, 60, 23, ERROR, "[macro expansion error]"
						+NL+ MACRO_MSG_2 +NL+ MACRO_MSG_1)
			)
		);
	}
	
	@Test
	public void testCargoErrorMesages() throws Exception { testCargoErrorMesages$(); }
	public void testCargoErrorMesages$() throws Exception {
	
		RustBuildOutputParser buildParser = new TestsRustBuildOutputParser();
		
		testParseMessages(buildParser, 
			"Cargo.toml:17:15-18:0 expected `=`, but found `", 
			listFrom(msg(path("Cargo.toml"), 17, 15, 18, 0, ERROR, "expected `=`, but found `")));
	}
	
}