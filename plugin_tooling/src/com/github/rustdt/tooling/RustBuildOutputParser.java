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
import melnorme.lang.utils.parse.StringParseSource;


public abstract class RustBuildOutputParser extends BuildOutputParser {
	
	protected static final Pattern MESSAGE_LINE_Regex = Pattern.compile(
		"^([^:\\n]*):" + // file
		"(\\d*):((\\d*):)?" +// line:column
		"( (\\d*):(\\d*))?" + // end line:column
		" (warning|error):" + // column-end
		"\\s(.*)$" // error message
	);
	
	
	@Override
	protected void doParseLine(String outputLine, StringParseSource output) {
		if(!outputLine.contains(":")) {
			return; // Ignore line
		}
		
		Matcher matcher = MESSAGE_LINE_Regex.matcher(outputLine);
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
		
		
		while(true) {
			String nextLine = output.stringUntilNewline();
			
			if(nextLine.isEmpty() || MESSAGE_LINE_Regex.matcher(nextLine).matches()) {
				break;
			}
			
			if(nextLine.startsWith("error: aborting due to previous error")) {
				// We reached the end of messages, exhaust remaining tool output.
				while(output.consume() != -1) {
				}
				break;
			}
			
			// We assume this is a multi line message.
			output.consumeLine();
			
			// However, first try to determine if this is the source range component, 
			// which we don't need
			
			String thirdLine = output.stringUntilNewline();
			String thirdLineTrimmed = thirdLine.trim();
			if(thirdLineTrimmed.startsWith("^") && 
					(thirdLineTrimmed.endsWith("^") || thirdLineTrimmed.endsWith("~"))) {
				output.consumeLine();
				// dont add this message, or nextLine, to actual error message.
				break;
			}
			
			message += "\n" + nextLine;
		}
		
		addMessage(pathString, lineString, columnString, endLineString, endColumnString, messageTypeString, message);
	}
	
}