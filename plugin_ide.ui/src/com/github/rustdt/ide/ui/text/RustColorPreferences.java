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

import melnorme.lang.ide.ui.text.coloring.ColoringItemPreference;

// Note: the file /resources/e4-dark_sourcehighlighting.css needs to updated with changes made here, 
// such as key name changes, or the color defaults
public interface RustColorPreferences {
	
	String PREFIX = "editor.coloring."; 
	
	// Defaults mostly based from "pre.rust." of http://static.rust-lang.org/doc/master/rust.css
	
	ColoringItemPreference DEFAULT = new ColoringItemPreference(PREFIX + "default",
		new RGB(0, 0, 0), false, false);
	
	ColoringItemPreference COMMENTS = new ColoringItemPreference(PREFIX + "comment",
		new RGB(144, 144, 144), false, false);
	// DOC_COMMENTS uses a diff color than rust.css
	ColoringItemPreference DOC_COMMENTS = new ColoringItemPreference(PREFIX + "doc_comment",
		new RGB(65, 95, 185), false, false);
	
	
	ColoringItemPreference NUMBERS = new ColoringItemPreference(PREFIX + "number",
		new RGB(113, 140, 0), false, false);
	ColoringItemPreference CHARACTER = new ColoringItemPreference(PREFIX + "character",
		new RGB(113, 140, 0), false, false);
	ColoringItemPreference STRINGS = new ColoringItemPreference(PREFIX + "string",
		new RGB(113, 140, 0), false, false);
	
	ColoringItemPreference KEYWORDS = new ColoringItemPreference(PREFIX + "keyword",
		new RGB(140, 90, 168), true, false);
	ColoringItemPreference KEYWORDS_VALUES = new ColoringItemPreference(PREFIX + "keyword_literals",
		new RGB(140, 90, 168), false, false);
	ColoringItemPreference LIFETIME = new ColoringItemPreference(PREFIX + "lifetime",
		new RGB(183, 101, 20), false, false);
	ColoringItemPreference ATTRIBUTE = new ColoringItemPreference(PREFIX + "attribute",
		new RGB(200, 40, 40), false, false);
	ColoringItemPreference MACRO_CALL = new ColoringItemPreference(PREFIX + "macro_call",
		new RGB(62, 153, 159), false, false);
	
}