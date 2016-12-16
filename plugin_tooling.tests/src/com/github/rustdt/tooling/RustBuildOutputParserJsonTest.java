/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *     Pieter Penninckx - adapted to the case of error output in the Json format
 *******************************************************************************/
package com.github.rustdt.tooling;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.status.Severity.ERROR;
import static melnorme.utilbox.status.Severity.INFO;
import static melnorme.utilbox.status.Severity.WARNING;

import org.junit.Test;

import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.lang.utils.parse.StringCharSource;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;

public class RustBuildOutputParserJsonTest extends CommonRustMessageParserTest {
	
	public static class TestsRustBuildOutputParser extends RustBuildOutputParser2 {
		@Override
		protected void handleParseError(CommonException ce) {
			assertFail();
		}
	}
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		testParseMessages("", list());  // Empty
		
		testParseMessages(new TestsRustBuildOutputParser(), 
			"Compiling my_project v0.1.0 (file:///path/to/my_project)", 
			list()
		);
		
		testParseMessages_WithErrors("{\"message\":null}", list());
		testParseMessages_WithErrors("{\"message", list());
		
		testParseMessages(
			getClassResource("simple_warning.json"),
			list(msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH")));
		
		testParseMessages(
			getClassResource("simple_warning_and_error.json"),
			list(
				msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH"),
				msg(path("src/main.rs"), 2, 22, 2, 23, ERROR, "XXX")
			)
		);
		
		testParseMessages( 
			getClassResource("error_with_children.json"), 
			list(msg(path("src/main.rs"), 6, 9, 6, 10, ERROR, 
						"mismatched types [E0308]:\n"+ // message + [Error code]
						"expected type `()`\n found type `{integer}`\n" + // (message child 1, message child 2)
						"expected (), found integral variable" // Label from span 
				))
			); 
		
		testParseMessages(
			getClassResource("aborting_due_to_error.json"),
			list()
		);
		
		
		testParseMessages(
			getClassResource("error_with_multiple_spans.json"),
			list(
				msg(path("test.rs"), 7, 15, 7, 16, INFO, // First span. Not primary, so INFO
//					"cannot borrow `i` as mutable more than once at a time: " + // message
					"first mutable borrow occurs here" // Label from first span
				), 
				msg(path("test.rs"), 8, 15, 8, 16, ERROR, // Second span. Primary, so we copy the severity.
					"cannot borrow `i` as mutable more than once at a time [E0499]:\n"+ // message + [error code]
					"second mutable borrow occurs here" // Label from the second span.
				), 
				msg(path("test.rs"), 9, 1, 9, 2, INFO, // Third span. Not primary, so INFO
//					"cannot borrow `i` as mutable more than once at a time: " +  // message
					"first borrow ends here" // Label from the third span.
				) 
			)
		); 
		
		testParseMessages(
			getClassResource("error_with_single_child.json"),
			list(msg(path("test.rs"), 39, 2, 39, 3, ERROR, // Primary span, so copy the severity (ERROR)
						"the trait bound `u8: Tr` is not satisfied [E0277]:\n"+ // message + [error code] 
						"required by `x`\n" + // (message from child)
						"trait `u8: Tr` not satisfied" // Label of the span
				) 
			)
		);
		
		testParseMessages(
			getClassResource("main_function_not_found.json"),
			list(msg(path(""), 1, 1, 1, 1, ERROR, "main function not found"))
		);
		
		// Test macro errors
		String MSG_MISMATCHED_M = "mismatched types [E0308]:\n"+
			"expected type `std::option::Option<&usize>`\n"+
			"   found type `&{integer}`";
		
		testParseMessages(
			getClassResource("macro_error.json"),
			list(
				msg(path(""), 5, 22, 5, 33, ERROR, MSG_MISMATCHED_M
					+"\nexpected enum `std::option::Option`, found &{integer}"),
				msg(path("src/test.rs"), 331, 2, 331, 33, ERROR, 
//					"(in expansion of `assert_eq!`) "+
					MSG_MISMATCHED_M
				)
//				msg(path(""), 1, 1, 18, 71, ERROR, MSG_MISMATCHED_M)
			)
		);			
		testParseMessages(
			getClassResource("macro_error2.json"),
			list(
				msg(path(""), 5, 22, 5, 33, ERROR, MSG_MISMATCHED_M
					+"\nexpected enum `std::option::Option`, found &{integer}"
				),
				msg(path("src/test.rs"), 331, 2, 331, 33, ERROR, 
//					"(in expansion of `assert_eq!`) "+
					MSG_MISMATCHED_M
				)
//				msg(path(""), 1, 1, 18, 71, ERROR, MSG_MISMATCHED_M)
			)
		);
	}
	
	protected void testParseMessages_WithErrors(String stderr, Indexable<ToolSourceMessage> expected) 
		throws CommonException 
	{
		TestsRustBuildOutputParser buildProcessor = new TestsRustBuildOutputParser() {
			@Override
			protected void handleParseError(CommonException ce) {
			}
		};
		testParseMessages(buildProcessor, stderr, expected);
	}
	
	protected void testParseMessages(String stderr, Indexable<ToolSourceMessage> expected) throws CommonException {
		testParseMessages(new TestsRustBuildOutputParser(), stderr, expected);
	}
	
	protected void testParseMessages(
		RustBuildOutputParser2 buildProcessor, String stderr, Indexable<ToolSourceMessage> expected
	) throws CommonException {
		buildProcessor.parseStdErr(new StringCharSource(stderr));
		ArrayList2<ToolSourceMessage> buildMessages = buildProcessor.getBuildMessages(); 
		assertEqualIndexable(buildMessages, expected);
	}
	
	@Deprecated
	@Test
	public void testCargoErrorMesages() throws Exception { testCargoErrorMesages$(); }
	public void testCargoErrorMesages$() throws Exception {
	
		testParseMessages(
			new TestsRustBuildOutputParser(),
			"Cargo.toml:17:15-18:0 expected `=`, but found `", 
			list(msg(path("Cargo.toml"), 17, 15, 18, 0, ERROR, "expected `=`, but found `"))
		);
		
	}

}