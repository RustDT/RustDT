/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.text;

import org.eclipse.swt.graphics.RGB;

import melnorme.lang.ide.ui.text.coloring.TextStyling;
import melnorme.lang.ide.ui.text.coloring.ThemedTextStylingPreference;

public interface RustColorPreferences {
	
	String PREFIX = "editor.coloring."; 
	
	// Defaults mostly based from "pre.rust." of http://static.rust-lang.org/doc/master/rust.css
	
	ThemedTextStylingPreference DEFAULT = new ThemedTextStylingPreference(PREFIX + "default",
		new TextStyling(new RGB(  0,  0,  0), false, false),
		new TextStyling(new RGB(230,230,230), false, false));
	
	ThemedTextStylingPreference COMMENTS = new ThemedTextStylingPreference(PREFIX + "comment",
		new TextStyling(new RGB(144, 144, 144), false, false),
		new TextStyling(new RGB(144, 144, 144), false, false));
	// DOC_COMMENTS uses a diff color than rust.css
	ThemedTextStylingPreference DOC_COMMENTS = new ThemedTextStylingPreference(PREFIX + "doc_comment",
		new TextStyling(new RGB( 65, 95, 185), false, false),
		new TextStyling(new RGB(110,135, 205), false, false));
	
	
	ThemedTextStylingPreference NUMBERS = new ThemedTextStylingPreference(PREFIX + "number",
		new TextStyling(new RGB(113, 140, 0), false, false),
		new TextStyling(new RGB(113, 140, 0), false, false));
	ThemedTextStylingPreference CHARACTER = new ThemedTextStylingPreference(PREFIX + "character",
		new TextStyling(new RGB(113, 140, 0), false, false),
		new TextStyling(new RGB(113, 140, 0), false, false));
	ThemedTextStylingPreference STRINGS = new ThemedTextStylingPreference(PREFIX + "string",
		new TextStyling(new RGB(113, 140, 0), false, false),
		new TextStyling(new RGB(113, 140, 0), false, false));
	
	ThemedTextStylingPreference KEYWORDS = new ThemedTextStylingPreference(PREFIX + "keyword",
		new TextStyling(new RGB(140, 90, 168), true, false),
		new TextStyling(new RGB(140, 90, 168), true, false));
	ThemedTextStylingPreference KEYWORDS_VALUES = new ThemedTextStylingPreference(PREFIX + "keyword_literals",
		new TextStyling(new RGB(140, 90, 168), false, false),
		new TextStyling(new RGB(140, 90, 168), false, false));
	ThemedTextStylingPreference LIFETIME = new ThemedTextStylingPreference(PREFIX + "lifetime",
		new TextStyling(new RGB(183, 101, 20), false, false),
		new TextStyling(new RGB(183, 101, 20), false, false));
	ThemedTextStylingPreference ATTRIBUTE = new ThemedTextStylingPreference(PREFIX + "attribute",
		new TextStyling(new RGB(200, 40, 40), false, false),
		new TextStyling(new RGB(200, 40, 40), false, false));
	ThemedTextStylingPreference MACRO_CALL = new ThemedTextStylingPreference(PREFIX + "macro_call",
		new TextStyling(new RGB(62, 153, 159), false, false),
		new TextStyling(new RGB(62, 153, 159), false, false));
	
}