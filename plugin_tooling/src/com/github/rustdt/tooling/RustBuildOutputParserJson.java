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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import melnorme.utilbox.status.Severity;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

import melnorme.lang.tooling.toolchain.ops.BuildOutputParser2;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.utils.parse.LexingUtils;
import melnorme.lang.utils.parse.StringCharSource;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.IByteSequence;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public abstract class RustBuildOutputParserJson extends BuildOutputParser2 {
	
	@Override
	protected void handleNonZeroExitCode(ExternalProcessResult result) throws CommonException, OperationSoftFailure {
		// Do nothing
	}
	
	@Override
	protected IByteSequence getOutputFromProcessResult(ExternalProcessResult result) {
		return result.getStdErrBytes(); // Use StdErr instead of StdOut.
	}
	
	// One line of Json data can contain more than one ToolMessageData, but we can only return one `ToolMessageData`
	// from the method `parseMessageData`.
	// So we create a buffer of remaining data.
	protected String lineParsing = null; // The line we are currently working with. 
	protected Queue<ToolMessageData> buffer; // Invariant: if buffer.isEmpty() then lineParsing == null
	
	public RustBuildOutputParserJson() {
		super();
		this.buffer = new ArrayDeque<ToolMessageData>();
	}
	
	protected static final AbstractRustBuildOutputLineParser CARGO_OUTPUT_LINE_PARSER = 
			new CargoRustBuildOutputLineParser();
	
	@Override
	protected ToolMessageData parseMessageData(StringCharSource output) throws CommonException {
		// The call-site stops calling `parseMessageData` when the `output` parameter is exhausted.
		// So we may not completely exhaust the `output` variable when there are
		// still `ToolMessageDatas`-s in the buffer.
		ToolMessageData result = null;
		try {
			if (this.lineParsing != null) {
				// Because of the invariant associated to buffer, we know that this.buffer is not empty.
				result = this.buffer.remove();
			} else {
				this.lineParsing = LexingUtils.stringUntilNewline(output);
				
				if (this.lineParsing.startsWith("{")) {
					try {
						JsonObject messages = Json.parse(this.lineParsing).asObject();
						addMessagesToBuffer(messages);
						if (!this.buffer.isEmpty()) {
							result = this.buffer.remove();
						}
					}
					catch(ParseException|UnsupportedOperationException|NumberFormatException e) {
						throw createUnknownLineSyntaxError(this.lineParsing);
					}
				} else {
					result = CARGO_OUTPUT_LINE_PARSER.parseLine(this.lineParsing);
				}
			}
		}
		finally {
			// Restore the invariant for buffer.
			if (this.buffer.isEmpty()) {
				// No more data to return next time, we may clear the output.
				output.consumeAhead(this.lineParsing);
				output.consumeAhead(LexingUtils.determineNewlineSequenceAt(output, 0));
				this.lineParsing = null;
			}
		}
		return result;
	}
	
	protected boolean isMessageEnd(String message) {
		return message.startsWith("aborting due to ");
	}
	
	/** Add all compiler errors (if any) to the buffer in the order in which they appear.
	 * 
	 * @param messages The JsonObject to parse into messages.
	 * @throws CommonException when messageLine is not in the expected format.
	 * @throws UnsupportedOperationException when messageLine is not in the expected format.
	 * @throws NumberFormatException when messageLine is not in the expected format.
	 */
	protected void addMessagesToBuffer(JsonObject messages) 
			throws CommonException, UnsupportedOperationException, NumberFormatException {
		String messageText = messages.getString("message", null);
		if (messageText == null) {
			throw createUnknownLineSyntaxError(this.lineParsing);
		}
		if (isMessageEnd(messageText)) {
			return;
		}
		String severityLevel = messages.getString("level", Severity.WARNING.getLabel());
		String errorCode = getErrorCode(messages);
		JsonValue spans = messages.get("spans");
		if (spans == null) { // spans should at least be an empty array.
			throw createUnknownLineSyntaxError(this.lineParsing);
		}
		JsonArray spansArray = spans.asArray();
		ToolMessageData primary = null;
		for (JsonValue span : spansArray.values()) {
			JsonObject spanObject = span.asObject();
			boolean isPrimary = spanObject.getBoolean("is_primary", false); 
			ToolMessageData subMessage = toolMessageDataFromJsonObject(spanObject, messageText,
				severityLevel, errorCode, isPrimary);
			if (isPrimary) {
				primary = subMessage;
			} 
			this.buffer.add(subMessage);
		}
		if (primary == null) {
			// Output line contains no spans. Example: "no main function found".
			primary = toolMessageWithoutSpan(messageText, severityLevel);
			this.buffer.add(primary);
		}
		primary.messageText += getNotes(messages.get("children"));
	}
	
	protected ToolMessageData toolMessageDataFromJsonObject(JsonObject spanObject, String message, 
			String severityLevel, String errorCode, boolean isPrimary) 
			throws UnsupportedOperationException, NumberFormatException {
		
		ToolMessageData subMessage = new ToolMessageData();
		JsonValue expansion = spanObject.get("expansion");
		if (expansion != null && !expansion.isNull()) {
			JsonObject expansionSpan = expansion.asObject().get("span").asObject();
			subMessage = toolMessageDataFromJsonObject(expansionSpan, message, severityLevel, errorCode, isPrimary);
		} else {
			subMessage.lineString = Integer.toString(spanObject.getInt("line_start", -1));
			subMessage.endLineString = Integer.toString(spanObject.getInt("line_end", -1));
			subMessage.columnString = Integer.toString(spanObject.getInt("column_start", -1));
			subMessage.endColumnString = Integer.toString(spanObject.getInt("column_end", -1));
			subMessage.pathString = spanObject.getString("file_name", "");
			subMessage.messageText = message;
			subMessage.sourceBeforeMessageText = ""; // This does not seem to be used (?). TODO
			
			if (isPrimary) {
				// Avoid clutter and show the code (e.g. "[E0499]") only for the primary span.
				if (errorCode != null && !errorCode.isEmpty()) {
					subMessage.messageText = subMessage.messageText + " [" + errorCode + "]";
				}
				subMessage.messageTypeString = severityLevel;
			} else {
				subMessage.messageTypeString = Severity.INFO.getLabel();
			}
		}
		
		JsonValue label = spanObject.get("label");
		if (label != null && label.isString()) {
			subMessage.messageText = subMessage.messageText + ": " + label.asString();
		}
		
		return subMessage;
	}
	
	protected ToolMessageData toolMessageWithoutSpan(String message, String level) 
			throws UnsupportedOperationException {
		ToolMessageData subMessage = new ToolMessageData();
		subMessage.lineString = "1";
		subMessage.endLineString = "1";
		subMessage.columnString = "1";
		subMessage.endColumnString = "1";
		if ("main function not found".equals(message)) {
			subMessage.pathString = "src/main.rs"; // TODO: this is only a guess.
		} else {
			subMessage.pathString = "";
		}
		subMessage.messageText = message;
		subMessage.sourceBeforeMessageText = "";
		subMessage.messageTypeString = level;
		return subMessage;
	}
	
	protected String getNotes(JsonValue children) throws UnsupportedOperationException {
		if (children == null) {
			return "";
		}
		ArrayList<String> notes = new ArrayList<String>();
		try {
			JsonArray childrenArray = children.asArray();
			for(JsonValue child: childrenArray.values()) {
				JsonObject childObject = child.asObject();
				String childMessage = childObject.getString("message", "");
				if (childMessage != null && !childMessage.isEmpty()) {
					notes.add(childMessage);
				}
			}
			if (!notes.isEmpty()) {
				return " (" + String.join(", ", notes) + ")";
			}

		} catch (UnsupportedOperationException e){
			// I think we can do without the notes.
			// TODO: Maybe log a warning?
		}
		return "";
	}
	
	protected String getErrorCode(JsonObject messageLine) throws UnsupportedOperationException {
		String errorCode = "";
		JsonValue code = messageLine.get("code");
		if (code != null && code.isObject()) {
			errorCode = code.asObject().getString("code", "");
		}
		return errorCode;
	}
}