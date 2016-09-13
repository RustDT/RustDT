/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package LANG_PROJECT_ID.ide.core_text;

import static melnorme.lang.ide.core.TextSettings_Actual.LangPartitionTypes.ATTRIBUTE;

import org.junit.Test;

import com.github.rustdt.tooling.lexer.RustAttributeRule;

import melnorme.lang.ide.core.TextSettings_Actual.LangPartitionTypes;

public class RustPartitionScannerTest extends LangPartitionScannerTest {
	
	public void testBasic() throws Exception {
		testPartitions("foo = \"asdf\"; ", array(LangPartitionTypes.STRING));
	}
	
	@Test
	public void testAttribute() throws Exception { testAttribute$(); }
	public void testAttribute$() throws Exception {
		testPartitions("foo = #[ blah ] ", array(ATTRIBUTE));
		testPartitions("foo = #![ \"--]--\" ] ", array(ATTRIBUTE));
		testPartitions(getClassResourceAsString(RustAttributeRule.class, "attribute_sample.rs") + "//other", 
			array(ATTRIBUTE, LangPartitionTypes.LINE_COMMENT));
	}
	
}