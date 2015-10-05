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

import melnorme.lang.ide.ui.text.coloring.TextStylingPreference;

// Note: the file /resources/e4-dark_sourcehighlighting.css needs to updated with changes made here, 
// such as key name changes, or the color defaults
public interface RustColorPreferences {
	
	String PREFIX = "editor.coloring."; 
	
	// Defaults mostly based from "pre.rust." of http://static.rust-lang.org/doc/master/rust.css
	
	TextStylingPreference DEFAULT = new TextStylingPreference(PREFIX + "default",
		new RGB(0, 0, 0), false, false);
	
	TextStylingPreference COMMENTS = new TextStylingPreference(PREFIX + "comment",
		new RGB(144, 144, 144), false, false);
	// DOC_COMMENTS uses a diff color than rust.css
	TextStylingPreference DOC_COMMENTS = new TextStylingPreference(PREFIX + "doc_comment",
		new RGB(65, 95, 185), false, false);
	
	
	TextStylingPreference NUMBERS = new TextStylingPreference(PREFIX + "number",
		new RGB(113, 140, 0), false, false);
	TextStylingPreference CHARACTER = new TextStylingPreference(PREFIX + "character",
		new RGB(113, 140, 0), false, false);
	TextStylingPreference STRINGS = new TextStylingPreference(PREFIX + "string",
		new RGB(113, 140, 0), false, false);
	
	TextStylingPreference KEYWORDS = new TextStylingPreference(PREFIX + "keyword",
		new RGB(140, 90, 168), true, false);
	TextStylingPreference KEYWORDS_VALUES = new TextStylingPreference(PREFIX + "keyword_literals",
		new RGB(140, 90, 168), false, false);
	TextStylingPreference LIFETIME = new TextStylingPreference(PREFIX + "lifetime",
		new RGB(183, 101, 20), false, false);
	TextStylingPreference ATTRIBUTE = new TextStylingPreference(PREFIX + "attribute",
		new RGB(200, 40, 40), false, false);
	TextStylingPreference MACRO_CALL = new TextStylingPreference(PREFIX + "macro_call",
		new RGB(62, 153, 159), false, false);
	
}