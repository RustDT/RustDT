/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pieter Penninckx - initial implementation
 *     Bruno Medeiros - refactor, modifications
 *******************************************************************************/
package com.github.rustdt.tooling;

import java.io.IOException;
import java.io.Reader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.lang.tooling.toolchain.ops.ToolMessageData;
import melnorme.lang.tooling.toolchain.ops.ToolMessageParser;
import melnorme.lang.utils.gson.GsonHelper;
import melnorme.lang.utils.gson.JsonParserX;
import melnorme.lang.utils.gson.JsonParserX.JsonSyntaxExceptionX;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.status.Severity;

public class RustJsonMessageParser {
	
	protected final GsonHelper helper = new GsonHelper();
	protected final ToolMessageParser messageParseHelper = new ToolMessageParser();
	
	public ToolSourceMessage parseSourceMessage(ToolMessageData msgdata) throws CommonException {
		if(msgdata.pathString.startsWith("<") && msgdata.pathString.endsWith(">")) {
			// That path doesn't exist, it's a macro expansion.
			// Furthermore, on Windows it's not even a valid Path, so createMessage would fail
			// So we set it to empty:
			msgdata.pathString = "";
		}
		
		return messageParseHelper.createMessage(msgdata);
	}
	
	public ArrayList2<RustMainMessage> parseStructuredMessages(Reader reader) throws CommonException {
		// Only Json from now on. 
		// JsonReader creates an internal buffer of its reader.
		// There is no way we can know how much it has parsed.
		JsonReader jsonReader = JsonParserX.newReader(reader, true);
		return parseStructuredMessages(jsonReader);
	}
	
	public ArrayList2<RustMainMessage> parseStructuredMessages(JsonReader jsonReader) throws CommonException {
		ArrayList2<RustMainMessage> rustMessages = new ArrayList2<>();
		while(true) {
			try {
				RustMainMessage rustMessage = parseStructuredMessage(jsonReader);
				if(rustMessage == null) {
					jsonReader.close();
					break;
				}
				rustMessages.add(rustMessage);
			} catch (IOException ioe) {
				throw new CommonException("Unexpected IO Exception: ", ioe);
			}
		}
		return rustMessages;
	}
	
	protected RustMainMessage parseStructuredMessage(JsonReader jsonReader) throws CommonException, IOException {
		if(JsonParserX.isEndOfInput(jsonReader)) {
			return null;
		}
		try {
			JsonElement element = new JsonParserX().parse(jsonReader);
			return parseTopLevelRustMessage(element);
		}
		catch(JsonSyntaxExceptionX e) {
			throw new CommonException("JSON syntax error in output message: ",  e);
		}
	}
	
	
	/** Add all compiler errors (if any) to the list of build messages in the order in which they appear.
	 * 
	 * @param messsagesElement The JsonElement to transform into messages.
	 * @throws CommonException when messageLine is not in the expected format.
	 */
	protected RustMainMessage parseTopLevelRustMessage(JsonElement messsagesElement) throws CommonException {
		JsonObject messages = helper.asObject(messsagesElement);
		String messageText = helper.getString(messages, "message");
		
		String severityLevel = helper.getOptionalString(messages, "level");
		if (severityLevel == null) {
			severityLevel = Severity.WARNING.getLabel();
		}
		
		String errorCode = getErrorCode(messages);
		ArrayList2<String> notes = parseNotes(messages.get("children"));
		
		JsonArray spansArray = helper.getArray(messages, "spans");
		
		ArrayList2<RustMessage> spans = new ArrayList2<>();
		
		for (JsonElement spanElement : spansArray) {
			JsonObject spanObject = helper.asObject(spanElement);
			RustMessage message = parseToolMessageFromSpanObject(spanObject, severityLevel, false, "");
			spans.add(message);
		}
		
		ToolMessageData messageData = toolMessageWithoutSpan(messageText, severityLevel);
		
		ToolSourceMessage sourceMessage = parseSourceMessage(messageData);
		return new RustMainMessage(sourceMessage, errorCode, notes, spans);
	}
	
	protected RustMessage parseToolMessageFromSpanObject(
		JsonObject spanObject, 
		String severityLevel, 
		boolean overridePrimaryToTrue, 
		String macroDeclarationName
	) throws CommonException {
		
		ToolMessageData subMessage = toolMessageWithRange(spanObject);
		boolean isPrimary = true;
		if (!overridePrimaryToTrue) {
			isPrimary = helper.getBooleanOr(spanObject, "is_primary", false);
		}
		subMessage.sourceBeforeMessageText = ""; // This does not seem to be used (?). TODO
		
		if (isPrimary) {
			subMessage.messageTypeString = severityLevel;
		} else {
			subMessage.messageTypeString = Severity.INFO.getLabel();
		}
		
		String label = helper.getOptionalString(spanObject, "label");
		subMessage.messageText = StringUtil.nullAsEmpty(label);
		if(label == null && Severity.ERROR.getLabel().equalsIgnoreCase(subMessage.messageTypeString)) {
			subMessage.messageTypeString = Severity.INFO.getLabel();
		}
		
		RustMessage expansionMsg = null;
		RustMessage defSiteMsg = null;
		
		JsonObject expansion = helper.getOptionalObject(spanObject, "expansion");
		if (expansion != null) {
			String macroDeclarationNameInExpansion = helper.getOptionalString(expansion, "macro_decl_name");
			if (macroDeclarationNameInExpansion != null) {
				macroDeclarationName = macroDeclarationNameInExpansion;
			}
			
			JsonObject expansionSpan = helper.getObject(expansion, "span");
			expansionMsg = parseToolMessageFromSpanObject(
				expansionSpan, severityLevel, isPrimary, macroDeclarationName
			);
			
			JsonObject expansionDefSiteSpan = helper.getOptionalObject(expansion, "def_site_span");
			if(expansionDefSiteSpan != null) {
				defSiteMsg = parseToolMessageFromSpanObject(expansionDefSiteSpan, severityLevel, isPrimary, "");
			}
		}
		
		return new RustSubMessage(parseSourceMessage(subMessage), isPrimary, expansionMsg, defSiteMsg);
	}
	
	protected ToolMessageData toolMessageWithRange(JsonObject span) throws CommonException {
		ToolMessageData subMessage = new ToolMessageData();
		subMessage.lineString = Integer.toString(helper.getIntegerOr(span, "line_start", -1));
		subMessage.endLineString = Integer.toString(helper.getIntegerOr(span, "line_end", -1));
		subMessage.columnString = Integer.toString(helper.getIntegerOr(span, "column_start", -1));
		subMessage.endColumnString = Integer.toString(helper.getIntegerOr(span, "column_end", -1));
		subMessage.pathString = helper.getStringOr(span, "file_name", "");
		return subMessage;
	}
	
	protected ToolMessageData toolMessageWithoutSpan(String message, String level) {
		ToolMessageData subMessage = new ToolMessageData();
		subMessage.lineString = "1";
		subMessage.endLineString = "1";
		subMessage.columnString = "1";
		subMessage.endColumnString = "1";
		subMessage.pathString = "";
		subMessage.messageText = message;
		subMessage.sourceBeforeMessageText = "";
		subMessage.messageTypeString = level;
		return subMessage;
	}
	
	protected ArrayList2<String> parseNotes(JsonElement children) throws CommonException {
		ArrayList2<String> notes = new ArrayList2<>();
		if (children == null || !children.isJsonArray()) {
			return notes;
		}
		JsonArray childrenArray = children.getAsJsonArray();
		if (childrenArray.size() == 0) {
			return notes;
		}
		for(JsonElement child: childrenArray) {
			if (child.isJsonObject()) {
				String childMessageString = helper.getOptionalString(child.getAsJsonObject(), 
					"message");
				if (childMessageString != null && !childMessageString.isEmpty()) {
					notes.add(childMessageString);
				}
			}
		}
		return notes;
	}
	
	protected String getErrorCode(JsonObject messageLine) throws CommonException {
		JsonObject codeObject = helper.getOptionalObject(messageLine, "code");
		if (codeObject != null) {
			String errorCode = helper.getOptionalString(codeObject, "code");
			if (errorCode != null) {
				return errorCode;
			}
		}
		return "";
	}
	
}