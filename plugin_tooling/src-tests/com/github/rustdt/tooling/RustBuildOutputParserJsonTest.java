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
import static melnorme.utilbox.core.CoreUtil.listFrom;
import static melnorme.utilbox.status.Severity.ERROR;
import static melnorme.utilbox.status.Severity.WARNING;
import static melnorme.utilbox.status.Severity.INFO;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.utilbox.core.CommonException;

public class RustBuildOutputParserJsonTest extends RustBuildOutputParserTest {
	
	public class TestsRustBuildOutputParserJson extends RustBuildOutputParserJson {
		@Override
		protected void handleParseError(CommonException ce) {
			assertFail();
		}
	}
		
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		RustBuildOutputParserJson buildParser = new TestsRustBuildOutputParserJson();
		RustBuildOutputParserJson buildParser_allowParseErrors = new RustBuildOutputParserJson() {
			@Override
			protected void handleParseError(CommonException ce) {
			}
		};
		
		testParseMessages(buildParser, "", listFrom());  // Empty
		
		testParseMessages(buildParser, "Compiling my_project v0.1.0 (file:///path/to/my_project)", listFrom());
		
		{
			testParseMessages(buildParser_allowParseErrors, "{\"message\":null}", listFrom());
			testParseMessages(buildParser_allowParseErrors, "{\"message", listFrom());
		}
		
		testParseMessages(buildParser,
			getClassResource("simple_warning.json"),
			listFrom(msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH")));
		
		testParseMessages(buildParser,
				getClassResource("simple_warning_and_error.json"),
				listFrom(msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH"),
						 msg(path("src/main.rs"), 2, 22, 2, 23, ERROR, "XXX")));
		
		testParseMessages(buildParser, 
			getClassResource("error_with_children.json"), 
			listFrom(msg(path("src/main.rs"), 6, 9, 6, 10, ERROR, 
						"mismatched types [E0308]: "+ // message + [Error code]
						"expected (), found integral variable " + // Label from span 
						"(expected type `()`,  found type `{integer}`)"))); // (message child 1, message child 2)
		
		testParseMessages(buildParser,
			getClassResource("aborting_due_to_error.json"),
			listFrom());
		
		
		testParseMessages(buildParser,
			getClassResource("error_with_multiple_spans.json"),
			listFrom(msg(path("test.rs"), 7, 15, 7, 16, INFO, // First span. Not primary, so INFO
							"cannot borrow `i` as mutable more than once at a time: " + // message
							"first mutable borrow occurs here"), // Label from first span
					msg(path("test.rs"), 8, 15, 8, 16, ERROR, // Second span. Primary, so we copy the severity.
							"cannot borrow `i` as mutable more than once at a time [E0499]: "+ // message + [error code]
							"second mutable borrow occurs here"), // Label from the second span.
					msg(path("test.rs"), 9, 1, 9, 2, INFO, // Third span. Not primary, so INFO
							"cannot borrow `i` as mutable more than once at a time: " +  // message
							"first borrow ends here"))); // Label from the third span.
		
		testParseMessages(buildParser,
			getClassResource("error_with_single_child.json"),
			listFrom(msg(path("test.rs"), 39, 2, 39, 3, ERROR, // Primary span, so copy the severity (ERROR)
						"the trait bound `u8: Tr` is not satisfied [E0277]: "+ // message + [error code] 
						"trait `u8: Tr` not satisfied " + // Label of the span
						"(required by `x`)")) // (message from child)
				);
		
		// If the main function is not found, we naively assume that it is in the `src/main.rs` file.
		// TODO: find out in which file the compiler looks for the main function.
		testParseMessages(buildParser,
			getClassResource("main_function_not_found.json"),
			listFrom(msg(path("src/main.rs"), 1, 1, 1, 1, ERROR, "main function not found")));
		
		testParseMessages(buildParser,
			getClassResource("macro_error.json"),
			listFrom(msg(path("<std macros>"), 5, 22, 5, 33, ERROR, "mismatched types [E0308]: "+
					"expected enum `std::option::Option`, found &{integer} "+
					"("+
						"expected type `std::option::Option<&usize>`, "+
						"   found type `&{integer}`"+
					")"),
					msg(path("src/test.rs"), 331, 2, 331, 33, ERROR, "mismatched types "+
					"(in expansion of `assert_eq!`) "+
					"[E0308]: "+
					"expected enum `std::option::Option`, found &{integer} "+
					"("+
						"expected type `std::option::Option<&usize>`, "+
						"   found type `&{integer}`"+
					")"),
					msg(path("<std macros>"), 1, 1, 18, 71, ERROR, "[macro expansion error] mismatched types [E0308]: "+
					"expected enum `std::option::Option`, found &{integer} "+
					"("+
						"expected type `std::option::Option<&usize>`, "+
						"   found type `&{integer}`"+
					")")));
		testParseMessages(buildParser,
				getClassResource("start_to_end.json"),
				listFrom(msg(path("src/main.rs"), 3, 5, 3, 6, ERROR, "mismatched types [E0308]: "+
					"expected (), found integral variable "+
					"("+
						"expected type `()`, " +
						"   found type `{integer}`"+
					")")));

	}
	
	protected void testParseMessages(RustBuildOutputParserJson buildProcessor, String stderr, List<?> expected) 
			throws CommonException {
		ArrayList<ToolSourceMessage> buildMessages = buildProcessor.parseOutput(stderr);
		assertEquals(buildMessages, expected);
	}
	
	@Test
	public void testCargoErrorMesages() throws Exception { testCargoErrorMesages$(); }
	public void testCargoErrorMesages$() throws Exception {
	
		RustBuildOutputParserJson buildParser = new TestsRustBuildOutputParserJson();
		
		testParseMessages(buildParser, 
			"Cargo.toml:17:15-18:0 expected `=`, but found `", 
			listFrom(msg(path("Cargo.toml"), 17, 15, 18, 0, ERROR, "expected `=`, but found `")));
	}

}