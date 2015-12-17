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

import static melnorme.lang.tooling.structure.StructureElementKind.VAR;

import org.junit.Test;

import melnorme.lang.tooling.ElementAttributes;
import melnorme.lang.tooling.ast.ParserError;
import melnorme.lang.tooling.ops.AbstractStructureParser_Test;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.lang.tooling.structure.StructureElementKind;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.StringUtil;


public class RustParseDescribeParser_Test extends AbstractStructureParser_Test {
	
	@Override
	protected RustParseDescribeParser createStructureParser() {
		return new RustParseDescribeParser(null, defaultSource);
	}
	
	public static String elemString(String kind, String name, String attribs, String type, String... children) {
		return 
			kind + "{" + quoteString(name) + "{ @0 @14 } { @0 @5 }" + attribs + type + 
			StringUtil.collToString(children, "\n")
			+ "}"
		;
	}
	
	public StructureElement elem(String name, StructureElementKind elementKind,
			ElementAttributes elementAttributes, String type, Indexable<StructureElement> children) {
		return elem(name, srAt(0, 14), srAt(0, 5), elementKind, elementAttributes, type, children);
	}
	
	protected void testParseDescribe(String describeOutputContent, Indexable<ParserError> parserProblems,  
			StructureElement... expectedElements)
			throws CommonException {
		String describeOutput = "RUST_PARSE_DESCRIBE 0.1 { \n" + describeOutputContent + "\n}";
		
		testParseStructure(describeOutput, parserProblems, expectedElements);
	}
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		
		testParseDescribe("", 
			list(), 
			array());
		
		
		testParseDescribe(
			"Function" + "{ func{ @0 @14 } { @0 @5 } {} {} }",
			
			list(), 
			array(
				elem("func", srAt(0, 14), srAt(0, 5), StructureElementKind.FUNCTION, null, null, list())
			)
		);
		
		
		/* ----------------- test kinds ----------------- */
		
		testParseDescribe(
			elemString("Var", "var", "{}", "{}") +
			elemString("Function", "function", "{}", "{}") +
			elemString("Struct", "struct", "{}", "{}") +
			elemString("Impl", "impl", "{}", "{}") +
			elemString("Trait", "trait", "{}", "{}") +
			elemString("Enum", "enum", "{}", "{}") +
			elemString("EnumVariant", "enum_vrt", "{}", "{}") +
			elemString("TypeAlias", "ty_alias", "{}", "{}") +
			elemString("ExternCrate", "ec", "{}", "{}") +
			elemString("Mod", "mod", "{}", "{}") +
			elemString("Use", "use", "{}", "{}"),
			
			list(), 
			array(
				elem("var"      , StructureElementKind.VAR, null, null, list()),
				elem("function" , StructureElementKind.FUNCTION, null, null, list()),
				elem("struct"   , StructureElementKind.STRUCT, null, null, list()),
				elem("impl"     , StructureElementKind.IMPL, null, null, list()),
				elem("trait"    , StructureElementKind.TRAIT, null, null, list()),
				elem("enum"     , StructureElementKind.ENUM, null, null, list()),
				elem("enum_vrt" , StructureElementKind.ENUM_VARIANT, null, null, list()),
				elem("ty_alias" , StructureElementKind.TYPE_ALIAS, null, null, list()),
				elem("ec"       , StructureElementKind.EXTERN_CRATE, null, null, list()),
				elem("mod"      , StructureElementKind.MOD, null, null, list()),
				elem("use"      , StructureElementKind.USE, null, null, list())
			)
		);
		
		// Test invalid element kind
		verifyThrows(() -> {
			testParseDescribe(elemString("XXX", "func", "{}", "{}"), list(), array());
		}, null, "Unknown element kind `XXX`");
		
		/* ----------------- test SourceRange ----------------- */
		
		testParseDescribe(
			"Var { var1 { @0 @10 } { @1 @14} {} {} }" +
			"Var { var2 { @5     } { @0    } {} {} }" +
			"Var { var3 { 0:0 0:0  } { 0:3 0:8 } {} {} }" +
			"Var { var4 { 1:0 1:10 } { 1:5 3:5 } {} {} }",
			
			list(), 
			array(
				elem("var1", srAt(0, 10), srAt(1,14), VAR, null, null, list()),
				elem("var2", srAt(5, 5) , srAt(0, 0), VAR, null, null, list()),
				elem("var3", srAt(pos(0, 0), pos(0, 0)), srAt(pos(0, 3), pos(0, 8)), VAR, null, null, list()),
				elem("var4", srAt(pos(1, 0), pos(1, 10)), srAt(pos(1, 5), pos(3, 5)), VAR, null, null, list())
			)
		);
		verifyThrows(() -> {
			testParseDescribe("Var { var1 {  } { } {} {} }", list(), array());
		}, null, "Missing source range");
		
		/* ----------------- test attrib ----------------- */
		
		testParseDescribe(
			elemString("Function", "func", "{pub}", "{}"),
			
			list(), 
			array(
				elem("func", StructureElementKind.FUNCTION, att(), null, list())
			)
		);
		verifyThrows(() -> {
			testParseDescribe(elemString("Function", "func", "{xx zzz}", "{}"), list(), array());
		}, null, "Unknown protection `xx`");
		verifyThrows(() -> {
			testParseDescribe(elemString("Function", "func", "{pub zzz}", "{}"), list(), array());
		}, null, "Unknown attribute");
		
		
		/* ----------------- test type ----------------- */
		testParseDescribe(
			elemString("Function", "func", "{}", "\"(u32) -> &str\""),
			
			list(), 
			array(
				elem("func", StructureElementKind.FUNCTION, null, "(u32) -> &str", list())
			)
		);
		verifyThrows(() -> {
			testParseDescribe(elemString("Function", "func", "{}", "{blah}"), list(), array());
		}, null, "Unknown element type");
		
		
		/* ----------------- test children ----------------- */
		
		testParseDescribe(
			elemString("Struct", "struct", "{}", "{}",
				elemString("Var", "var1", "{}", "{}"),
				elemString("Var", "var2", "{}", "{}")
			) +
			elemString("Impl", "impl", "{}", "{}",
				elemString("Function", "function", "{}", "{}")
			),
			
			list(), 
			array(
				elem("struct", StructureElementKind.STRUCT, null, null, list(
					elem("var1", StructureElementKind.VAR, null, null, list()),
					elem("var2", StructureElementKind.VAR, null, null, list())
				)),
				elem("impl", StructureElementKind.IMPL, null, null, list(
					elem("function", StructureElementKind.FUNCTION, null, null, list())
				))
			)
		);
		
	}
	
}