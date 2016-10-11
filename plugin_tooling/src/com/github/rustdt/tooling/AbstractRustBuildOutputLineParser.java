/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *     Pieter Penninckx - small refactoring + documentation
 *******************************************************************************/
package com.github.rustdt.tooling;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import melnorme.lang.tooling.toolchain.ops.BuildOutputParser2.ToolMessageData;

/**
 * Simple regexp-based parser for (error) output lines of the rust build process.
 *
 */
public abstract class AbstractRustBuildOutputLineParser {
	
	protected abstract Pattern getPattern();
	
	/** Get a matcher for parsing an error output line.
	 * 
	 * @param line: the line to which the matcher applies.
	 * @return a matcher with 9 groups:
	 * 	1: the path (filename),
	 *  2: the line number of beginning,
	 * (3: unused),
	 *  4: the column number of the beginning,
	 * (5: unused),
	 *  6: the line number of the end,
	 *  7: the column number of the end,
	 *  8: the severity, where "note" corresponds to "info",
	 *  9: the message.
	 *  
	 *  May not return `null`.
	 */
	protected Matcher getMatcher(String line) {
		return this.getPattern().matcher(line);
	}
	
	/**
	 * @param line: the line to be parsed.
	 * @return true when the given line can be parsed with this object.
	 */
	public boolean canParseLine(String line) {
		Matcher matcher = this.getMatcher(line);
		return matcher.matches();
	}
	
	/**
	 * Parse the given `line` into a `ToolMessageData`.
	 * @param line: the error output line.
	 * @return The `ToolMessageData` corresponding the information in the error output line.
	 *         Returns `null` when the `line` parameter cannot be parsed.
	 */
	public ToolMessageData parseLine(String line) {
		Matcher matcher = this.getMatcher(line);
		if(!matcher.matches()) {
			return null;
		}
		
		ToolMessageData msgData = new ToolMessageData();
		
		msgData.pathString = matcher.group(1);
		msgData.lineString = matcher.group(2);
		msgData.columnString = matcher.group(4);
		
		msgData.endLineString = matcher.group(6);
		msgData.endColumnString = matcher.group(7);
		
		msgData.messageTypeString = matcher.group(8);
		if(areEqual(msgData.messageTypeString, "note")) {
			
			msgData.messageTypeString = "info";
		}
		
		msgData.messageText = matcher.group(9);
		msgData.sourceBeforeMessageText = line.substring(0, line.length() - msgData.messageText.length());
		
		return msgData;
	}
}