/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pieter Penninckx - initial implementation
 *******************************************************************************/
package com.github.rustdt.tooling;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;

import java.io.Reader;

import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.lang.tooling.toolchain.ops.BuildOutputParser3;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.tooling.toolchain.ops.ToolMessageData;
import melnorme.lang.utils.parse.LexingUtils;
import melnorme.lang.utils.parse.StringCharSource;
import melnorme.lang.utils.parse.StringCharSource.StringCharSourceReader;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public abstract class RustBuildOutputParser2 extends BuildOutputParser3 {
	
	@Override
	protected void handleNonZeroExitCode(ExternalProcessResult result) throws CommonException, OperationSoftFailure {
		// Do nothing
	}
		
	protected static final AbstractRustBuildOutputLineParser CARGO_OUTPUT_LINE_PARSER = 
			new CargoRustBuildOutputLineParser();
	
	@Override
	public void parseStdOut(StringCharSource stdout) throws CommonException {
		StringCharSourceReader reader = stdout.toReader();
		parseStdOut(reader);
	}
	
	public void parseStdOut(Reader reader) throws CommonException {
		ArrayList2<CargoMessage> cargoMessages = new CargoMessageParser(reader).parseCargoMessages();
		
		for (CargoMessage cargoMessage : cargoMessages) {
			ArrayList2<ToolSourceMessage> toolMessages = cargoMessage.message.retrieveToolMessages();
			for(ToolSourceMessage flatMessage : toolMessages) {
				addBuildMessage(flatMessage);
			}
		}
	}
	
	@Override
	public void parseStdErr(StringCharSource output) throws CommonException {
		// Deprecated functionality, remove support for this at some point:
		try {
			if(output.lookahead() == '{') {
				RustBuildOutputParserJson msgParser = new RustBuildOutputParserJson();
				ArrayList2<RustMainMessage> rustMessages = msgParser.parseStructuredMessages(output.toReader());
				
				for (RustMainMessage rustMessage : rustMessages) {
					for(ToolSourceMessage flatMessage : rustMessage.retrieveToolMessages()) {
						addBuildMessage(flatMessage);
					}
				}
			} else {
				while(output.hasCharAhead()) {
					String lineParsing = LexingUtils.consumeLine(output);
					ToolMessageData cargo_tool_message = CARGO_OUTPUT_LINE_PARSER.parseLine(lineParsing);
					if (cargo_tool_message != null) {
						addBuildMessage(cargo_tool_message);
					}
				}
			}
		} catch (CommonException ce) {
			handleMessageParseError(ce);
		}
	}
	
	@Override
	public ArrayList2<ToolSourceMessage> parseOutput(StringCharSource output) throws CommonException {
		throw assertFail();
	}
	
	@Override
	protected ToolMessageData parseMessageData(StringCharSource output) throws CommonException {
		// This function is not called from within this class because we override doParseToolMessage.
		throw assertFail();
	}
	
}