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
		
		{
			// TODO: write more tests in case of errors.
			testParseMessages(buildParser_allowParseErrors, "{\"message", listFrom());
		}
		
		testParseMessages(buildParser,
			"{\"message\":\"BLAH BLAH BLAH\",\"level\":\"warning\"," + 
				"\"spans\":[" +
					"{"+
						"\"file_name\":\"src/main.rs\",\"byte_start\":0,\"byte_end\":10,\"line_start\":1," + 
						"\"line_end\":3,\"column_start\":2,\"column_end\":17,\"is_primary\":true," + 
						"\"label\":null,\"suggested_replacement\":null,"+
						"\"expansion\":null"+
					"}"+
				"],"+
				"\"children\":[],\"rendered\":null" +
			"}",
			listFrom(msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH")));
		
		testParseMessages(buildParser,
				"{\"message\":\"BLAH BLAH BLAH\",\"level\":\"warning\"," + 
					"\"spans\":["+
						"{"+
							"\"file_name\":\"src/main.rs\",\"byte_start\":0,\"byte_end\":10,\"line_start\":1," + 
							"\"line_end\":3,\"column_start\":2,\"column_end\":17,\"is_primary\":true," + 
							"\"text\":["+
								"{\"text\":\"fn a() {\",\"highlight_start\":1,\"highlight_end\":9},"+
								"{\"text\":\"}\",\"highlight_start\":1,\"highlight_end\":2}"+
							"],"+
							"\"label\":null,\"suggested_replacement\":null,\"expansion\":null"+
						"}"+
					"]," +
					"\"children\":[],\"rendered\":null"+
				"}\n" +  
				"{\"message\":\"XXX\",\"code\":null,\"level\":\"error\"," + 
					"\"spans\":["+
						"{"+
							"\"file_name\":\"src/main.rs\",\"byte_start\":47,\"byte_end\":48,\"line_start\":2,"+
							"\"line_end\":2,\"column_start\":22,\"column_end\":23,\"is_primary\":true,"+
							"\"text\":["+
								"{\"text\":\"const STRING2: str = ;\",\"highlight_start\":22,\"highlight_end\":23}" +
							"],"+
							"\"label\":null,\"suggested_replacement\":null,\"expansion\":null"+
						"}"+
					"],"+
					"\"children\":[],\"rendered\":null"+
				"}",
				listFrom(msg(path("src/main.rs"), 1, 2, 3, 17, WARNING, "BLAH BLAH BLAH"),
						 msg(path("src/main.rs"), 2, 22, 2, 23, ERROR, "XXX")));
		
		testParseMessages(buildParser, 
			"{\"message\":\"mismatched types\","+
				"\"code\":{"+
					"\"code\":\"E0308\","+
					"\"explanation\":\"\\n"+
						"This error occurs when the compiler was unable to infer the concrete type of a\\n" +
						"variable. It can occur for several cases, the most common of which is a\\n"+
						"mismatch in the expected type that the compiler inferred for a variable\'s\\n"+
						"initializing expression, and the actual type explicitly assigned to the\\nvariable.\\n"+
						"\\nFor example:\\n\\n```compile_fail,E0308\\n"+
						"let x: i32 = \\\"I am not a number!\\\";\\n// ~~~ ~~~~~~~~~~~~~~~~~~~~\\n// | |\\n"+
						"// | initializing expression;\\n"+
						"// | compiler infers type `&str`\\n// |\\n// type `i32` assigned to variable `x`\\n```\\n" +
						"\\nAnother situation in which this occurs is when you attempt to use the `try!`\\n"+
						"macro inside a function that does not return a `Result<T, E>`:\\n\\n"+
						"```compile_fail,E0308\\nuse std::fs::File;\\n\\n" + 
						"fn main() {\\n let mut f = try!(File::create(\\\"foo.txt\\\"));\\n}\\n```\\n\\n"+
						"This code gives an error like this:\\n\\n"+
						"```text\\n<std macros>:5:8: 6:42 error: mismatched types:\\n expected `()`,"+
						"\\n found `core::result::Result<_, _>`\\n (expected (),\\n "+
						"found enum `core::result::Result`) [E0308]\\n```\\n"+
						"\\n`try!` returns a `Result<T, E>`, and so the function must. But `main()` has\\n"+
						"`()` as its return type, hence the error.\\n\""+
				"}," + 
				"\"level\":\"error\"," + 
				"\"spans\":["+
					"{"+
						"\"file_name\":\"src/main.rs\",\"byte_start\":40,\"byte_end\":41," + 
						"\"line_start\":6,\"line_end\":6,\"column_start\":9,\"column_end\":10,\"is_primary\":true," + 
						"\"text\":[{\"text\":\"\\treturn 0;\",\"highlight_start\":9,\"highlight_end\":10}]," + 
						"\"label\":\"expected (), found integral variable\",\"suggested_replacement\":null,"+
						"\"expansion\":null"+
					"}"+
				"]," +
				"\"children\":["+
					"{"+
						"\"message\":\"expected type `()`\",\"code\":null,\"level\":\"note\","+
						"\"spans\":[],\"children\":[],\"rendered\":null"+
					"},{"+
						"\"message\":\" found type `{integer}`\",\"code\":null,\"level\":\"note\","+
						"\"spans\":[],\"children\":[],\"rendered\":null"+
					"}"+
				"]," + 
				"\"rendered\":null"+
			"}\n" + 
			"{"+
				"\"message\":\"aborting due to 2 previous errors\",\"code\":null,\"level\":\"error\","+
				"\"spans\":[],\"children\":[],\"rendered\":null"+
			"}\n", 
			listFrom(msg(path("src/main.rs"), 6, 9, 6, 10, ERROR, 
						"mismatched types [E0308]: "+ // message + [Error code]
						"expected (), found integral variable " + // Label from span 
						"(expected type `()`,  found type `{integer}`)"))); // (message child 1, message child 2)
		
		testParseMessages(buildParser,
			"{\"message\":\"aborting due to previous error\",\"code\":null,\"level\":\"error\",\"spans\":[],"+
			"\"children\":[],\"rendered\":null}",
			listFrom());
		
		
		testParseMessages(buildParser,
			"{\"message\":\"cannot borrow `i` as mutable more than once at a time\"," + 
			"\"code\":{\"code\":\"E0499\"," + 
				"\"explanation\":\"\\nA variable was borrowed as mutable more than once. Erroneous code example:\\n\\n"+
				"```compile_fail,E0499\\nlet mut i = 0;\\nlet mut x = &mut i;\\nlet mut a = &mut i;\\n"+
				"// error: cannot borrow `i` as mutable more than once at a time\\n```\\n\\n"+
				"Please note that in rust, you can either have many immutable references, or one\\n"+
				"mutable reference. Take a look at\\n"+
				"https://doc.rust-lang.org/stable/book/references-and-borrowing.html for more\\n"+
				"information. Example:\\n\\n\\n```\\nlet mut i = 0;\\nlet mut x = &mut i; // ok!\\n\\n// or:\\n"+
				"let mut i = 0;\\nlet a = &i; // ok!\\nlet b = &i; // still ok!\\nlet c = &i; // still ok!\\n```\\n"+
				"\"},"+
			"\"level\":\"error\",\"spans\":["+
				"{"+
					"\"file_name\":\"test.rs\",\"byte_start\":74,\"byte_end\":75,\"line_start\":7,\"line_end\":7,"+
					"\"column_start\":15,\"column_end\":16,\"is_primary\":false,"+
					"\"text\":[{\"text\":\"\\tlet j = &mut i;\",\"highlight_start\":15,\"highlight_end\":16}],"+
					"\"label\":\"first mutable borrow occurs here\","+
					"\"suggested_replacement\":null,\"expansion\":null"+
				"},{" + 
					"\"file_name\":\"test.rs\",\"byte_start\":91,\"byte_end\":92,\"line_start\":8,\"line_end\":8,"+
					"\"column_start\":15,\"column_end\":16,\"is_primary\":true,"+
					"\"text\":[{\"text\":\"\\tlet k = &mut i;\",\"highlight_start\":15,\"highlight_end\":16}],"+
					"\"label\":\"second mutable borrow occurs here\","+
					"\"suggested_replacement\":null,\"expansion\":null"+
				"},{"+
					"\"file_name\":\"test.rs\",\"byte_start\":94,\"byte_end\":95,\"line_start\":9,\"line_end\":9,"+
					"\"column_start\":1,\"column_end\":2,\"is_primary\":false,"+
					"\"text\":[{\"text\":\"}\",\"highlight_start\":1,\"highlight_end\":2}],"+
					"\"label\":\"first borrow ends here\",\"suggested_replacement\":null,\"expansion\":null"+
				"}"+
			"]," + 
			"\"children\":[],\"rendered\":null}",
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
			"{\"message\":\"the trait bound `u8: Tr` is not satisfied\"," +
				"\"code\":{\"code\":\"E0277\",\"explanation\":\"blablabla\"}," +
				"\"level\":\"error\"," +
				"\"spans\":[" +
					"{" +
						"\"file_name\":\"test.rs\",\"byte_start\":329,\"byte_end\":330,\"line_start\":39," +
						"\"line_end\":39,\"column_start\":2,\"column_end\":3,\"is_primary\":true," +
						"\"text\":[{\"text\":\"\\tx(0_u8);\",\"highlight_start\":2,\"highlight_end\":3}]," +
						"\"label\":\"trait `u8: Tr` not satisfied\",\"suggested_replacement\":null,\"expansion\":null" +
					"}" + 
				"]," + 
				"\"children\":[{" +
						"\"message\":\"required by `x`\",\"code\":null,\"level\":\"note\"," +
						"\"spans\":[],\"children\":[],\"rendered\":null" +
				"}],\"rendered\":null}",
			listFrom(msg(path("test.rs"), 39, 2, 39, 3, ERROR, // Primary span, so copy the severity (ERROR)
						"the trait bound `u8: Tr` is not satisfied [E0277]: "+ // message + [error code] 
						"trait `u8: Tr` not satisfied " + // Label of the span
						"(required by `x`)")) // (message from child)
				);
		
		// If the main function is not found, we naively assume that it is in the `src/main.rs` file.
		// TODO: find out in which file the compiler looks for the main function.
		testParseMessages(buildParser,
			"{" +
				"\"message\":\"main function not found\",\"code\":null,\"level\":\"error\"," +
				"\"spans\":[],\"children\":[],\"rendered\":null" +
			"}",
			listFrom(msg(path("src/main.rs"), 1, 1, 1, 1, ERROR, "main function not found")));
		
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