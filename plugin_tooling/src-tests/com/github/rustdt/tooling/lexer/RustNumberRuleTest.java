/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *     Konstantin Salikhov - test cases for lexing rule
 *******************************************************************************/
package com.github.rustdt.tooling.lexer;

import melnorme.lang.tests.NumberRuleTest;
import melnorme.lang.tooling.parser.lexer.ILexingRule;

public class RustNumberRuleTest extends NumberRuleTest {
	
	@Override
	protected ILexingRule createLexingRule() {
		return new RustNumberLexingRule();
	}
	
	@Override
	protected void testInteger() {
		super.testInteger();
		
		testRule("1u8", 3);
		testRule("1i8", 3);
		testRule("2u16", 4);
		testRule("2i16", 4);
		testRule("0b1u32", 6);
		testRule("0o7i32", 6);
		testRule("0xAu64", 6);
		testRule("0xAi64", 6);
	}
	
	@Override
	protected void testFloats() {
		super.testFloats();
		
		testRule("123.1f32", 8);
		testRule("123.2f64", 8);
		
		testRule("0.2u32", 3);
	}
	
}