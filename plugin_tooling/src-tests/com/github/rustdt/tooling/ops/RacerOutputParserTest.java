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
import melnorme.lang.tooling.completion.LangCompletionProposal;
import melnorme.lang.tooling.completion.LangCompletionResult;
import melnorme.utilbox.core.CommonException;

import org.junit.Test;


public class RacerOutputParserTest extends CommonToolingTest {
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		int offset = 10;
		
		RacerOutputParser buildParser = new RacerOutputParser(offset) {
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
				new LangCompletionProposal(offset, "BufReader", 0),
				new LangCompletionProposal(offset, "BufRead", 0)
			)
		);
		
		testParseOutput(buildParser, 
			"PREFIX 4,6,pr\n" +
			"MATCH BufReader;BufReader;32;11;/RustProject/src/xpto.rs;Struct;pub struct BufReader<R> {\n"
			, 
			listFrom(
				new LangCompletionProposal(offset-2, "BufReader", 2)
			)
		);
	}
	
	protected void testParseOutput(RacerOutputParser parser, String output, List<?> expected) throws CommonException {
		LangCompletionResult result = parser.parse(output);
		assertAreEqualLists((List<?>) result.getProposals(), expected);
	}
	
}