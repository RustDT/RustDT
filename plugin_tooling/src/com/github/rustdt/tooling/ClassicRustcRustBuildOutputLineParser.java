/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *     Pieter Penninckx - small refactoring
 *******************************************************************************/
package com.github.rustdt.tooling;

import java.util.regex.Pattern;

/**
 * Simple regexp-based parser for (error) output lines of rustc up to and including version 1.12.
 * 
 * Parses only single-line errors or the first line of multi-line errors. Can only parse lines in the genre of
 * 
 *  test.rs:4:15: 4:16 error: cannot borrow `a` as mutable more than once at a time [E0499]
 *	
 * where the column number (in this case 15) and the end (in this case: 4:16) may be omitted. 
 */
public class ClassicRustcRustBuildOutputLineParser extends AbstractRustBuildOutputLineParser {

	protected static final Pattern MESSAGE_LINE_Regex = Pattern.compile(
			"^([^:\\n]*):" + // file
			"(\\d*):((\\d*):)?" +// line:column
			"( (\\d*):(\\d*))?" + // end line:column
			" (warning|error|note):" + // type
			"\\s(.*)$" // error message
		);
	
	@Override
	protected Pattern getPattern() {
		return MESSAGE_LINE_Regex;
	}

}