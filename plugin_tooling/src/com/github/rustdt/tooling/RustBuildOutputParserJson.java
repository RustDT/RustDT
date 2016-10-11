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

import java.io.IOException;
import java.util.ArrayList;

import melnorme.utilbox.status.Severity;

import com.google.gson.JsonParser;
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
		try {
			while (output.hasCharAhead()) {
				if (output.lookahead() == '{') {
					parseJsonMessages(output);
				} else {
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
	
	protected void parseJsonMessages(StringCharSource output) throws CommonException {
		// Only Json from now on. 
		// JsonReader creates an internal buffer of its reader.
		// There is no way we can know how much it has parsed.
		JsonParser parser = new JsonParser();
		StringCharSourceReader reader = output.toReader();
		JsonReader jsonReader = new JsonReader(reader);
		jsonReader.setLenient(true);
		try {
			while (jsonReader.peek() == JsonToken.BEGIN_OBJECT) {
				try {
					JsonElement element = parser.parse(jsonReader);
					addMessagesFromJsonObject(element);
				}
				catch(JsonParseException e) {
					throw new CommonException("Invalid output JSON object: ",  e);
				}
			}
		} catch (IOException ioe) {
			throw new CommonException("Unexpected IO Exception");
		}
	}
		
	@Override
	protected ToolMessageData parseMessageData(StringCharSource output) throws CommonException {
		// This function is not called from within this class because we override doParseToolMessage.
		throw assertFail();
	}
	
	protected boolean isMessageEnd(String message) {
		return message.startsWith("aborting due to ");
	}
	
	/** Add all compiler errors (if any) to the list of build messages in the order in which they appear.
	 * 
	 * @param jsonElement The JsonElement to transform into messages.
	 * @throws CommonException when messageLine is not in the expected format.
	 */
	protected void addMessagesFromJsonObject(JsonElement jsonElement) throws CommonException {
		if (!jsonElement.isJsonObject()) {
			throw createUnknownLineSyntaxError(jsonElement.toString());
		}
		JsonObject messages = jsonElement.getAsJsonObject();
		String messageText = JsonHelper.getStringMemberFromJsonObject(messages, "message");
		if (messageText == null) {
			throw createUnknownLineSyntaxError(jsonElement.toString());
		}
		if (isMessageEnd(messageText)) {
			return;
		}
		
		String severityLevel = JsonHelper.getStringMemberFromJsonObject(messages, "level");
		if (severityLevel == null) {
			severityLevel = Severity.WARNING.getLabel();
		}
		
		String errorCode = getErrorCode(messages);
		String notes = getNotes(messages.get("children"));
		
		JsonElement spans = messages.get("spans");
		if (spans == null || !spans.isJsonArray()) { // spans should at least be an empty array.
			throw createUnknownLineSyntaxError(jsonElement.toString());
		}
		JsonArray spansArray = spans.getAsJsonArray();
		if (spansArray.size() == 0) {
			// Output line contains no spans. Example: "no main function found".
			ToolMessageData without_span = toolMessageWithoutSpan(messageText, severityLevel, notes);
			addBuildMessage(without_span);
		} else {
			for (JsonElement span : spansArray) {
				if (!span.isJsonObject()) {
					throw createUnknownLineSyntaxError(jsonElement.toString());
				}
				JsonObject spanObject = span.getAsJsonObject();
				addToolMessageFromSpanObject(spanObject, messageText, severityLevel, errorCode, notes, "", false, "");
			}
		}
	}
	
	protected void addToolMessageFromSpanObject(JsonObject spanObject, String message, 
			String severityLevel, String errorCode, String notes, String defaultLabel, 
			boolean overridePrimaryToTrue, String macroDeclarationName) 
			throws CommonException {
		
		ToolMessageData subMessage = toolMessageWithRange(spanObject);
		boolean isPrimary = true;
		if (!overridePrimaryToTrue) {
			isPrimary = JsonHelper.getBooleanMemberFromJsonObject(spanObject, "is_primary", false);
		}
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
		
		String label = JsonHelper.getStringMemberFromJsonObject(spanObject, "label");
		if (label != null) {
			defaultLabel = label;
		}
		if (defaultLabel != null && !"".equals(defaultLabel)) {
			subMessage.messageText = subMessage.messageText + ": " + defaultLabel;
		}
		
		if (isPrimary) {
			subMessage.messageText = subMessage.messageText + notes;
		}
		
		addBuildMessage(subMessage);
		
		JsonObject expansion = JsonHelper.getObjectMemberFromJsonObject(spanObject, "expansion");
		if (expansion != null) {
			String macroDeclarationNameInExpansion = JsonHelper.getStringMemberFromJsonObject(expansion, 
				"macro_decl_name");
			if (macroDeclarationNameInExpansion != null) {
				macroDeclarationName = macroDeclarationNameInExpansion; 
			}
			JsonObject expansionSpan = JsonHelper.getObjectMemberFromJsonObject(expansion, "span");
			if (spanObject == null) {
				throw createUnknownLineSyntaxError(expansion.toString());
			}
			addToolMessageFromSpanObject(expansionSpan, message, severityLevel, errorCode, notes, 
				defaultLabel, isPrimary, macroDeclarationName);
			JsonObject expansionDefSiteSpan = JsonHelper.getObjectMemberFromJsonObject(expansion, "def_site_span");
			if (expansionDefSiteSpan == null) {
				throw createUnknownLineSyntaxError(expansion.toString());
			}
			addToolMessageFromSpanObject(expansionDefSiteSpan, "[macro expansion error] " + message, severityLevel, 
				errorCode, notes, defaultLabel, isPrimary, "");
		}
	}
	
	protected ToolMessageData toolMessageWithRange(JsonObject span) {
		ToolMessageData subMessage = new ToolMessageData();
		subMessage.lineString = Integer.toString(JsonHelper.getIntMemberFromJsonObject(span, "line_start", -1));
		subMessage.endLineString = Integer.toString(JsonHelper.getIntMemberFromJsonObject(span, "line_end", -1));
		subMessage.columnString = Integer.toString(JsonHelper.getIntMemberFromJsonObject(span, "column_start", -1));
		subMessage.endColumnString = Integer.toString(JsonHelper.getIntMemberFromJsonObject(span, "column_end", -1));
		subMessage.pathString = JsonHelper.getStringMemberFromJsonObject(span, "file_name");
		if (subMessage.pathString == null) {
			subMessage.pathString = "";
		}
		return subMessage;
	}
	
	protected ToolMessageData toolMessageWithoutSpan(String message, String level, String notes) 
			throws UnsupportedOperationException {
		ToolMessageData subMessage = new ToolMessageData();
		subMessage.lineString = "1";
		subMessage.endLineString = "1";
		subMessage.columnString = "1";
		subMessage.endColumnString = "1";
		subMessage.pathString = "";
		subMessage.messageText = message + notes;
		subMessage.sourceBeforeMessageText = "";
		subMessage.messageTypeString = level;
		return subMessage;
	}
	
	protected String getNotes(JsonElement children) {
		if (children == null || !children.isJsonArray()) {
			return "";
		}
		JsonArray childrenArray = children.getAsJsonArray();
		if (childrenArray.size() == 0) {
			return "";
		}
		ArrayList<String> notes = new ArrayList<String>();
		for(JsonElement child: childrenArray) {
			if (child.isJsonObject()) {
				String childMessageString = JsonHelper.getStringMemberFromJsonObject(child.getAsJsonObject(), 
					"message");
				if (childMessageString != null && !childMessageString.isEmpty()) {
					notes.add(childMessageString);
				}
			}
		}
		return " (" + String.join(", ", notes) + ")";
	}
	
	protected String getErrorCode(JsonObject messageLine) {
		JsonObject codeObject = JsonHelper.getObjectMemberFromJsonObject(messageLine, "code");
		if (codeObject != null) {
			String errorCode = JsonHelper.getStringMemberFromJsonObject(codeObject, "code");
			if (errorCode != null) {
				return errorCode;
			}
		}
		return "";
	}
}