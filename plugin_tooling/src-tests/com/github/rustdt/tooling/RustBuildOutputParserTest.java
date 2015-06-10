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
package com.github.rustdt.tooling;


import static melnorme.lang.tooling.data.StatusLevel.ERROR;
import static melnorme.lang.tooling.data.StatusLevel.WARNING;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.CoreUtil.listFrom;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tooling.data.StatusLevel;
import melnorme.lang.tooling.ops.SourceLineColumnRange;
import melnorme.lang.tooling.ops.ToolSourceMessage;
import melnorme.utilbox.core.CommonException;

import org.junit.Test;


public class RustBuildOutputParserTest extends CommonToolingTest {
	
	protected static ToolSourceMessage msg(Path path, int line, int column, int endLine, int endColumn, 
			StatusLevel level, String errorMessage) {
		
		ToolSourceMessage msg = new ToolSourceMessage(new SourceLineColumnRange(path, line, column, endLine, endColumn), 
					level, errorMessage);
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
		RustBuildOutputParser buildParser = new RustBuildOutputParser() {
			@Override
			protected void handleLineParseError(CommonException ce) {
				assertFail();
			}
		};
		RustBuildOutputParser buildParser_allowParseErrors = new RustBuildOutputParser() {
			@Override
			protected void handleLineParseError(CommonException ce) {
			}
		};

		
		testParseMessages(buildParser, "", listFrom());  // Empty
		
		// Test that this line is ignored without reporting a syntax error.
		testParseMessages(buildParser, "asdfsdaf/asdfsd", listFrom());
		
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
			"src/main.rs:1 const STRING2 : str  = xxx;" +"\n"+
			"                                     ^~~" +"\n"+
			"src/main.rs:2:2: 3:10 error: XXX\n"+
			"src/main.rs:1 const STRING2 : str  = ;" +"\n"+
			"                                     ^"
			, 
			listFrom(
				msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH"),
				msg(path("src/main.rs"), 2, 2, 3, 10, ERROR, "XXX")
			)
		);
		
		// Test actual multiline message
		testParseMessages(buildParser, 
			"src/main.rs:1:2: 3:17 error: " + MULTILINE_MESSAGE + "\n" +
			"src/main.rs:1: warning: xxx" + MULTILINE_MESSAGE,
			listFrom(
				msg(path("src/main.rs"), 1, 2, 3, 17, ERROR, MULTILINE_MESSAGE),
				msg(path("src/main.rs"), 1, -1, -1, -1, WARNING, "xxx" + MULTILINE_MESSAGE)
			)
		);
		
	}
	
	protected void testParseMessages(RustBuildOutputParser buildProcessor, String stderr, List<?> expected) 
			throws CommonException {
		ArrayList<ToolSourceMessage> buildMessages = buildProcessor.parseMessages(stderr);
		assertEquals(buildMessages, expected);
	}
	
}