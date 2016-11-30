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
package com.github.rustdt.tooling;

import java.nio.file.Path;

import melnorme.lang.tests.CommonToolingTest;
import melnorme.lang.tooling.common.SourceLineColumnRange;
import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.utilbox.status.Severity;


public class CommonRustMessageParserTest extends CommonToolingTest {
	
	public static SourceLineColumnRange range(int line_1, int column_1, int endLine, int endColumn) {
		return new SourceLineColumnRange(line_1, column_1, endLine, endColumn);
	}
	
	public static SourceLineColumnRange noRange() {
		return new SourceLineColumnRange(-1, -1);
	}
	
	public static ToolSourceMessage msg(
		Path path, int line, int column, int endLine, int endColumn, Severity severity, String errorMessage
	) {
		ToolSourceMessage msg = new ToolSourceMessage(path, 
			new SourceLineColumnRange(line, column, endLine, endColumn), 
			severity, errorMessage);
		msg.toString();
		return msg;
	}
	
	protected static ToolSourceMessage msg(
		String path, int line, int column, int endLine, int endColumn, Severity severity, String errorMessage
	) {
		return msg(path(path), line, column, endLine, endColumn, severity, errorMessage);
	}
	
	protected static ToolSourceMessage msg(
		String path, Severity severity, String errorMessage
	) {
		return msg(path(path), -1, -1, -1, -1, severity, errorMessage);
	}
	
}