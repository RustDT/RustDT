/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import melnorme.lang.tooling.ops.BuildOutputParser;


public abstract class RustBuildOutputParser extends BuildOutputParser {
	
	protected static final Pattern ERROR_LINE_Regex = Pattern.compile(
		"^([^:\\n]*):" + // file
		"(\\d*):((\\d*):)?" +// line:column
		"( (\\d*):(\\d*))?" + // end line:column
		" (warning|error):" + // column-end
		"\\s(.*)$" // error message
	);
	
	
	@Override
	protected void doParseLine(String outputLine) {
		if(!outputLine.contains(":")) {
			return; // Ignore line
		}
		
		Matcher matcher = ERROR_LINE_Regex.matcher(outputLine);
		if(!matcher.matches()) {
			handleUnknownLineSyntax(outputLine);
			return;
		}
		
		String pathString = matcher.group(1);
		String lineString = matcher.group(2);
		String columnString = matcher.group(4);
		
		String endLineString = matcher.group(6);
		String endColumnString = matcher.group(7);
		
		String messageTypeString = matcher.group(8);
		
		String message = matcher.group(9);
		
		addMessage(pathString, lineString, columnString, endLineString, endColumnString, messageTypeString, message);
	}
	
}