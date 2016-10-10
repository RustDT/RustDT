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

import java.io.IOException;
import java.util.ArrayList;

import melnorme.utilbox.status.Severity;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonToken;

import melnorme.lang.tooling.toolchain.ops.BuildOutputParser2;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.utils.parse.LexingUtils;
import melnorme.lang.utils.parse.StringCharSource;
import melnorme.lang.utils.parse.StringCharSource.StringCharSourceReader;
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
		JsonParser parser = new JsonParser();
		StringCharSourceReader reader = output.toReader();
		try {
			while (reader.hasCharAhead()) {
				String beginning = reader.lookaheadString(0, 1);
				if ("{".equals(beginning)) {
					JsonReader jsonReader = new JsonReader(reader);
					jsonReader.setLenient(true);
					// JsonReader creates an internal buffer of its reader.
					// There is no way we can know how much it has parsed.
					try {
						while (jsonReader.peek() == JsonToken.BEGIN_OBJECT) {
							try {
								JsonElement element = parser.parse(jsonReader);
								addMessagesFromJsonObject(element);
							}
							catch(JsonParseException e) {
								throw createUnknownLineSyntaxError(LexingUtils.consumeLine(reader));
							}
						}
					} catch (IOException ioe) {
						throw new CommonException("Unexpected IO Exception");
					}
				} else {
					String lineParsing = LexingUtils.consumeLine(reader);
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
	 */
	protected void addMessagesFromJsonObject(JsonElement jsonElement) throws CommonException {
		JsonObject messages = jsonElement.getAsJsonObject();
		if (!messages.has("message")) {
			throw createUnknownLineSyntaxError(jsonElement.toString());
		}
		String messageText = messages.getAsJsonPrimitive("message").getAsString();
		if (isMessageEnd(messageText)) {
			return;
		}
		
		String severityLevel = Severity.WARNING.getLabel();
		JsonPrimitive severityLevelPrimitive = messages.getAsJsonPrimitive("level");
		if (severityLevelPrimitive != null) {
			severityLevel = severityLevelPrimitive.getAsString(); 
		}
		
		String errorCode = getErrorCode(messages);
		String notes = getNotes(messages.get("children"));
		
		JsonElement spans = messages.get("spans");
		if (spans == null) { // spans should at least be an empty array.
			throw createUnknownLineSyntaxError(jsonElement.toString());
		}
		JsonArray spansArray = spans.getAsJsonArray();
		if (!spansArray.iterator().hasNext()) {
			// Output line contains no spans. Example: "no main function found".
			ToolMessageData without_span = toolMessageWithoutSpan(messageText, severityLevel, notes);
			addBuildMessage(without_span);
		} else {
			for (JsonElement span : spansArray) {
				JsonObject spanObject = span.getAsJsonObject();
				addToolMessageFromSpanObject(spanObject, messageText, severityLevel, errorCode, notes, "", false, "");
			}
		}
	}
	
	protected void addToolMessageFromSpanObject(JsonObject spanObject, String message, 
			String severityLevel, String errorCode, String notes, String defaultLabel, 
			boolean overridePrimaryToTrue, String macroDeclarationName) 
			throws UnsupportedOperationException, NumberFormatException, CommonException {
		
		ToolMessageData subMessage = toolMessageWithRange(spanObject);
		boolean isPrimary = overridePrimaryToTrue ? true: spanObject.getAsJsonPrimitive("is_primary").getAsBoolean();
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
		
		JsonElement label = spanObject.get("label");
		if (label != null && label.isJsonPrimitive()) {
			JsonPrimitive labelPrimitive = label.getAsJsonPrimitive();
			if (labelPrimitive.isString()) {
				defaultLabel = label.getAsString();
			}
		}
		if (defaultLabel != null && !"".equals(defaultLabel)) {
			subMessage.messageText = subMessage.messageText + ": " + defaultLabel;
		}
		
		if (isPrimary) {
			subMessage.messageText = subMessage.messageText + notes;
		}
		
		addBuildMessage(subMessage);
		
		JsonElement expansion = spanObject.get("expansion");
		if (expansion != null && !expansion.isJsonNull()) {
			JsonObject expansionObject = expansion.getAsJsonObject();
			JsonPrimitive macroDeclarationNamePrimitive = expansionObject.getAsJsonPrimitive("macro_decl_name");
			if (macroDeclarationNamePrimitive != null) {
				macroDeclarationName = macroDeclarationNamePrimitive.getAsString(); 
			}
			JsonObject expansionSpan = expansionObject.getAsJsonObject("span");
			addToolMessageFromSpanObject(expansionSpan, message, severityLevel, errorCode, notes, 
				defaultLabel, isPrimary, macroDeclarationName);
			JsonObject expansionDefSiteSpan = expansionObject.get("def_site_span").getAsJsonObject();
			addToolMessageFromSpanObject(expansionDefSiteSpan, "[macro expansion error] " + message, severityLevel, 
				errorCode, notes, defaultLabel, isPrimary, "");
		}
	}
	
	protected ToolMessageData toolMessageWithRange(JsonObject spanObject) {
		ToolMessageData subMessage = new ToolMessageData();
		subMessage.lineString = Integer.toString(spanObject.getAsJsonPrimitive("line_start").getAsInt());
		subMessage.endLineString = Integer.toString(spanObject.getAsJsonPrimitive("line_end").getAsInt());
		subMessage.columnString = Integer.toString(spanObject.getAsJsonPrimitive("column_start").getAsInt());
		subMessage.endColumnString = Integer.toString(spanObject.getAsJsonPrimitive("column_end").getAsInt());
		subMessage.pathString = spanObject.getAsJsonPrimitive("file_name").getAsString();
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
	
	protected String getNotes(JsonElement children) {
		if (children == null) {
			return "";
		}
		ArrayList<String> notes = new ArrayList<String>();
		JsonArray childrenArray = children.getAsJsonArray();
		for(JsonElement child: childrenArray) {
			JsonElement childMessageElement = child.getAsJsonObject().get("message");
			if (childMessageElement != null) {
				String childMessageString = childMessageElement.getAsString();
				if (childMessageString != null && !childMessageString.isEmpty()) { 
					notes.add(childMessageString);
				}
			}
		}
		if (!notes.isEmpty()) {
			return " (" + String.join(", ", notes) + ")";
		}
		// TODO: error catching
		return "";
	}
	
	protected String getErrorCode(JsonObject messageLine) {
		String errorCode = "";
		JsonElement code = messageLine.get("code");
		if (code != null && code.isJsonObject()) {
			JsonObject errorCodeObject = code.getAsJsonObject();
			if (errorCodeObject.has("code")) {
				errorCode = errorCodeObject.getAsJsonPrimitive("code").getAsString();
			}
		}
		return errorCode;
	}
}