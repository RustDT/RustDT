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

import melnorme.lang.tooling.toolchain.ops.BuildOutputParser2.ToolMessageData;
import melnorme.utilbox.status.Severity;

/**
 * Simple regexp-based parser for (error) output lines of cargo.
 *
 */
public class CargoRustBuildOutputLineParser extends AbstractRustBuildOutputLineParser {
	protected static final Pattern CARGO_MESSAGE_Regex = Pattern.compile(
			"^([^:\\n]*):" + // file
			"(\\d*):((\\d*))?" +// line:column
			"(-(\\d*):(\\d*))?" + // end line:column
			"()" + // type and sep. Type is assumed to always be 'Error', we will override parseLine to fix this.
			"\\s(.*)$" // error message
		);
	
	@Override
	protected Pattern getPattern() {
		return CARGO_MESSAGE_Regex;
	}
	
	@Override
	public ToolMessageData parseLine(String line) {
		ToolMessageData result = super.parseLine(line);
		if (result != null) {
			result.messageTypeString = Severity.ERROR.getLabel();
		}
		return result;
	}

}