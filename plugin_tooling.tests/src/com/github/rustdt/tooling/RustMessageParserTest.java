/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
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
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import static melnorme.utilbox.status.Severity.ERROR;
import static melnorme.utilbox.status.Severity.INFO;

import java.io.StringReader;

import org.junit.Test;

import melnorme.lang.tooling.common.SourceLineColumnRange;
import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;

public class RustMessageParserTest extends CommonRustMessageParserTest {
	
	public static final String CANNOT_BORROW_MUT_MORE_THAN = 
		"cannot borrow `xpto` as mutable more than once at a time";
	
	public static final SourceLineColumnRange PARENT_MSG_RANGE = range(1, 1, 1, 1); // Maybe should be -1 ?

	public static final RustMainMessage MSG_Simple = new RustMainMessage(
		new ToolSourceMessage(path(""), PARENT_MSG_RANGE, ERROR, "unresolved name `xpto`"),
		"E0425",
		null, 
		list(
			new RustSubMessage(msg("src/main.rs", 8, 19, 8, 23, ERROR, "unresolved name"))
		)
	);
	public static final RustMainMessage MSG_MismatchedTypes = new RustMainMessage(
		new ToolSourceMessage(path(""), PARENT_MSG_RANGE, ERROR, "mismatched types"),
		"E0308",
		list(
			"expected type `&MyTrait`",
			"   found type `{integer}`"
		),
		list(
			new RustSubMessage(msg("src/main.rs", 14, 9, 14, 12, ERROR, "expected &MyTrait, found integral variable"))
		)
	);
	public static final RustMainMessage MSG_CannotReborrow = new RustMainMessage(
		new ToolSourceMessage(path(""), PARENT_MSG_RANGE, ERROR, CANNOT_BORROW_MUT_MORE_THAN),
		"E0499",
		null, 
		list(
			new RustSubMessage(msg("src/main.rs", 6,19,6,23, INFO, "first mutable borrow occurs here"), false),
			new RustSubMessage(msg("src/main.rs", 8,19,8,23, ERROR, "second mutable borrow occurs here"), true),
			new RustSubMessage(msg("src/main.rs", 14,1,14,2, INFO, "first borrow ends here"), false)
		)
	);
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		testParseMessage(
			"", 
			null, 
			list()
		);
		
		testParseMessage(
			getClassResource("rustc_error_simple.json"), 
			MSG_Simple, 
			list(
				msg(path("src/main.rs"), 8, 19, 8, 23, ERROR, "unresolved name `xpto`"+ " [E0425]:"+
					subm("unresolved name"))
			)
		);
		
		String MSG_MISMATCHED_A = "mismatched types"+ " [E0308]:"+
			subm("expected type `&MyTrait`")+
			subm("   found type `{integer}`")+
			subm("expected &MyTrait, found integral variable");
		
		testParseMessage(
			getClassResource("rustc_error_with_notes.json"), 
			MSG_MismatchedTypes, 
			list(msg(path("src/main.rs"), 14, 9, 14, 12, ERROR, MSG_MISMATCHED_A))
		);
		
		testParseMessage(
			getClassResource("rustc_error_composite.json"), 
			MSG_CannotReborrow, 
			list(
				msg(path("src/main.rs"), 6, 19, 6, 23, INFO, "first mutable borrow occurs here"),
				msg(path("src/main.rs"), 8, 19, 8, 23, ERROR, CANNOT_BORROW_MUT_MORE_THAN + " [E0499]:" +
					subm("second mutable borrow occurs here")
				),
				msg(path("src/main.rs"), 14, 1, 14, 2, INFO, "first borrow ends here")
			)
		);
		
		
		String spanMessage = "expected bool, found integral variable";
		String MSG_MISMATCHED_B = "mismatched types"+ " [E0308]:"+
			subm("expected type `bool`")+
			subm("   found type `{integer}`");
		
		testParseMessage(
			getClassResource("rustc_error_macro.json"), 
			new RustMainMessage(
				new ToolSourceMessage(path(""), PARENT_MSG_RANGE, ERROR, "mismatched types"),
				"E0308",
				list(
					"expected type `bool`",
					"   found type `{integer}`"
				),
				list(new RustSubMessage(
					msg("", 5,22,5,33, ERROR, spanMessage), 
					true, 
					new RustSubMessage(msg("src/main.rs", 15,5,15,26, ERROR, "")), 
					new RustSubMessage(msg("", 1,1,18,71, ERROR, ""))
				))
			),
			list(
				msg(path(""), 5,22,5,33, ERROR, MSG_MISMATCHED_B+subm(spanMessage)),
				msg(path("src/main.rs"), 15,5,15,26, ERROR, MSG_MISMATCHED_B)
//				msg(path(""), 1,1,18,71, ERROR, MSG_MISMATCHED_B)
			)
		);
	}
	
	public String subm(String subMessage) {
		return "\n"+subMessage;
	}
	
	public void testParseMessage(
		String messageJson, RustMessage expected, Indexable<ToolSourceMessage> expectedSourceMessages
	) throws CommonException 
	{
		RustBuildOutputParserJson msgParser = new RustBuildOutputParserJson();
		ArrayList2<RustMainMessage> rustMessages = msgParser.parseStructuredMessages(new StringReader(messageJson));
		if(expected == null) {
			assertTrue(rustMessages.isEmpty());
			return;
		}
		
		RustMainMessage message = unwrapSingle(rustMessages);
		checkEquals(message, expected);
		
		assertEqualIndexable(message.retrieveToolMessages(), expectedSourceMessages);
	}
	
	public void checkEquals(RustMessage message, RustMessage expected) {
		if(!expected.equals(message)) {
			
			// Helper for the interactive debugger:
			assertAreEqual(message.sourceMessage, expected.sourceMessage);
			assertAreEqual(message.notes, expected.notes);
			checkIndexable(message.spans, expected.spans, this::checkEquals);
			if(message instanceof RustSubMessage && expected instanceof RustSubMessage) {
				checkSubMessage((RustSubMessage) message, (RustSubMessage) expected);
			}
			
			assertFail();
		}
	}
	
	public void checkSubMessage(RustSubMessage message, RustSubMessage expected) {
		assertAreEqual(message.defSiteMsg, expected.defSiteMsg);
		assertAreEqual(message.expansionMsg, expected.expansionMsg);
	}
	
}