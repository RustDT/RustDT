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

import java.util.ArrayList;
import java.util.List;

import melnorme.utilbox.status.Severity;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

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
		
	protected static final AbstractRustBuildOutputLineParser CARGO_OUTPUT_LINE_PARSER = 
			new CargoRustBuildOutputLineParser();
	
	@Override
	protected void doParseToolMessage(StringCharSource output) {
		try {
			String lineParsing = LexingUtils.consumeLine(output);
			
			if (lineParsing.startsWith("{")) {
				JsonParser parser = new JsonParser(); // Not yet used. Just to ensure that we can use it.
				try {
					addMessagesFromJsonObject(lineParsing);
				}
				catch(ParseException|UnsupportedOperationException|NumberFormatException e) {
					throw createUnknownLineSyntaxError(lineParsing);
				}
			} else {
				ToolMessageData cargo_tool_message = CARGO_OUTPUT_LINE_PARSER.parseLine(lineParsing);
				if (cargo_tool_message != null) {
					addBuildMessage(cargo_tool_message);
				}
			}
		} catch (CommonException ce) {
			handleMessageParseError(ce);
		}
	}
		
	@Override
	protected ToolMessageData parseMessageData(StringCharSource output) throws CommonException {
		return null; // This function is not called from within this class because we override doParseToolMessage.
	}
	
	protected boolean isMessageEnd(String message) {
		return message.startsWith("aborting due to ");
	}
	
	/** Add all compiler errors (if any) to the list of build messages in the order in which they appear.
	 * 
	 * @param messages The JsonObject to parse into messages.
	 * @throws CommonException when messageLine is not in the expected format.
	 * @throws UnsupportedOperationException when messageLine is not in the expected format.
	 * @throws NumberFormatException when messageLine is not in the expected format.
	 */
	protected void addMessagesFromJsonObject(String lineParsing) 
			throws CommonException, UnsupportedOperationException, NumberFormatException {
		JsonObject messages = Json.parse(lineParsing).asObject();
		String messageText = messages.getString("message", null);
		if (messageText == null) {
			throw createUnknownLineSyntaxError(lineParsing);
		}
		if (isMessageEnd(messageText)) {
			return;
		}
		String severityLevel = messages.getString("level", Severity.WARNING.getLabel());
		String errorCode = getErrorCode(messages);
		String notes = getNotes(messages.get("children"));
		
		JsonValue spans = messages.get("spans");
		if (spans == null) { // spans should at least be an empty array.
			throw createUnknownLineSyntaxError(lineParsing);
		}
		List<JsonValue> spansList = spans.asArray().values();
		if (spansList.isEmpty()) {
			// Output line contains no spans. Example: "no main function found".
			ToolMessageData without_span = toolMessageWithoutSpan(messageText, severityLevel, notes);
			addBuildMessage(without_span);
		} else {
			for (JsonValue span : spansList) {
				JsonObject spanObject = span.asObject();
				addToolMessageFromSpanObject(spanObject, messageText, severityLevel, errorCode, notes, "", false, "");
			}
		}
	}
	
	protected void addToolMessageFromSpanObject(JsonObject spanObject, String message, 
			String severityLevel, String errorCode, String notes, String defaultLabel, 
			boolean overridePrimaryToTrue, String macroDeclarationName) 
			throws UnsupportedOperationException, NumberFormatException, CommonException {
		
		ToolMessageData subMessage = toolMessageWithRange(spanObject);
		boolean isPrimary = overridePrimaryToTrue ? true: spanObject.getBoolean("is_primary", false);
		subMessage.messageText = message;
		subMessage.sourceBeforeMessageText = ""; // This does not seem to be used (?). TODO
		
		if (! "".equals(macroDeclarationName)) {
			subMessage.messageText = subMessage.messageText + " (in expansion of `" + macroDeclarationName + "`)";
		}
		
		if (isPrimary) {
			// Avoid clutter and show the code (e.g. "[E0499]") only for the primary span.
			if (errorCode != null && !errorCode.isEmpty()) {
				subMessage.messageText = subMessage.messageText + " [" + errorCode + "]";
			} 
			subMessage.messageTypeString = severityLevel;
		} else {
			subMessage.messageTypeString = Severity.INFO.getLabel();
		}
		
		JsonValue label = spanObject.get("label");
		if (label != null && label.isString()) {
			defaultLabel = label.asString();
		}
		if (defaultLabel != null && !"".equals(defaultLabel)) {
			subMessage.messageText = subMessage.messageText + ": " + defaultLabel;
		}
		
		if (isPrimary) {
			subMessage.messageText = subMessage.messageText + notes;
		}
		
		addBuildMessage(subMessage);
		
		JsonValue expansion = spanObject.get("expansion");
		if (expansion != null && !expansion.isNull()) {
			JsonObject expansionObject = expansion.asObject();
			macroDeclarationName = expansionObject.getString("macro_decl_name", "");
			JsonObject expansionSpan = expansionObject.get("span").asObject();
			addToolMessageFromSpanObject(expansionSpan, message, severityLevel, errorCode, notes, 
				defaultLabel, isPrimary, macroDeclarationName);
			JsonObject expansionDefSiteSpan = expansionObject.get("def_site_span").asObject();
			addToolMessageFromSpanObject(expansionDefSiteSpan, "[macro expansion error] " + message, severityLevel, 
				errorCode, notes, defaultLabel, isPrimary, "");
		}
	}
	
	protected ToolMessageData toolMessageWithRange(JsonObject spanObject) {
		ToolMessageData subMessage = new ToolMessageData();
		subMessage.lineString = Integer.toString(spanObject.getInt("line_start", -1));
		subMessage.endLineString = Integer.toString(spanObject.getInt("line_end", -1));
		subMessage.columnString = Integer.toString(spanObject.getInt("column_start", -1));
		subMessage.endColumnString = Integer.toString(spanObject.getInt("column_end", -1));
		subMessage.pathString = spanObject.getString("file_name", "");
		return subMessage;
	}
	
	protected ToolMessageData toolMessageWithoutSpan(String message, String level, String notes) 
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
		subMessage.messageText = message + notes;
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