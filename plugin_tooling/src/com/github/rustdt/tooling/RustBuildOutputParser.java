/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.CoreUtil.areEqual;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import melnorme.lang.tooling.ops.BuildOutputParser;
import melnorme.lang.tooling.ops.ToolSourceMessage;
import melnorme.lang.utils.parse.LexingUtils;
import melnorme.lang.utils.parse.StringParseSource;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.StringUtil;


public abstract class RustBuildOutputParser extends BuildOutputParser {
	
	@Override
	protected String getToolProcessName() {
		return "Cargo";
	}
	
	protected boolean isMessageEnd(String nextLine) {
		return nextLine.startsWith("error: aborting due to ");
	}
	
	@Override
	protected CompositeToolMessageData parseMessageData(StringParseSource output) throws CommonException {
		
		String outputLine = LexingUtils.consumeLine(output);
		
		if(isMessageEnd(outputLine)) {
			// We reached the end of messages, exhaust remaining tool output.
			while(output.consumeAny()) {
			}
			return null;
		}
		
		CompositeToolMessageData messageData = parseSimpleMessage(outputLine);
		if(messageData == null) {
			Matcher matcher = CARGO_MESSAGE_Regex.matcher(outputLine);
			
			if(matcher.matches()) {
				return parseFromMatcher(outputLine, matcher);
			} else {
				throw createUnknownLineSyntaxError(outputLine);	
			}
		}
		
		parseMultiLineMessageText(output, messageData);
		
		parseAssociatedMessage(output, messageData);
		
		return messageData;
	}
	
	protected static final Pattern CARGO_MESSAGE_Regex = Pattern.compile(
		"^([^:\\n]*):" + // file
		"(\\d*):((\\d*))?" +// line:column
		"(-(\\d*):(\\d*))?" + // end line:column
		"()" + // type and sep
		"\\s(.*)$" // error message
	);
	
	protected static final Pattern MESSAGE_LINE_Regex = Pattern.compile(
		"^([^:\\n]*):" + // file
		"(\\d*):((\\d*):)?" +// line:column
		"( (\\d*):(\\d*))?" + // end line:column
		" (warning|error|note):" + // type
		"\\s(.*)$" // error message
	);
	
	public CompositeToolMessageData parseSimpleMessage(String line) {
		Matcher matcher = MESSAGE_LINE_Regex.matcher(line);
		if(!matcher.matches()) {
			return null;
		}
		
		return parseFromMatcher(line, matcher);
	}
	
	public CompositeToolMessageData parseFromMatcher(String line, Matcher matcher) {
		CompositeToolMessageData msgData = new CompositeToolMessageData();
		
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
	
	protected void parseMultiLineMessageText(StringParseSource output, CompositeToolMessageData msg) {
		while(true) {
			String lineAhead = LexingUtils.stringUntilNewline(output);
			
			if(lineAhead.isEmpty() || MESSAGE_LINE_Regex.matcher(lineAhead).matches()) {
				break;
			}
			if(isMessageEnd(lineAhead)) {
				break;
			}
			
			// Then this should be a multi line message text.
			LexingUtils.consumeLine(output);
			
			// However, first try to determine if this is the source snippet text, 
			// which we don't need to be part of message text
			
			String thirdLine = LexingUtils.stringUntilNewline(output);
			String thirdLineTrimmed = thirdLine.trim();
			if(thirdLineTrimmed.startsWith("^") && 
					(thirdLineTrimmed.endsWith("^") || thirdLineTrimmed.endsWith("~"))) {
				LexingUtils.consumeLine(output);
				// dont add this message, or nextLine, to actual error message.
				break;
			}
			
			msg.messageText += "\n" + lineAhead;
		}
	}
	
	protected void parseAssociatedMessage(StringParseSource output, CompositeToolMessageData msg)
			throws CommonException {
		String lineAhead = LexingUtils.stringUntilNewline(output);
		
		msg.simpleMessageText = msg.messageText;
		
		CompositeToolMessageData nextMessage = parseSimpleMessage(lineAhead);
		if(nextMessage != null) {
			
			if(isTemplateInstantiationMessage(nextMessage)) {
				nextMessage = parseMessageData(output);
				msg.nextMessage = nextMessage;
				msg.messageText += "\n" + nextMessage.getFullMessageSource(); 
			}
			
		}
	}
	
	public static class CompositeToolMessageData extends ToolMessageData {
		
		public CompositeToolMessageData nextMessage;
		public String simpleMessageText;
		
		public String getFullMessageSource() {
			assertNotNull(sourceBeforeMessageText);
			assertNotNull(messageText);
			
			return sourceBeforeMessageText + messageText;
		}
		
		@Override
		public String toString() {
			return getFullMessageSource();
		}
		
	}
	
	protected boolean isTemplateInstantiationMessage(ToolMessageData nextMessage) {
		String messageText = nextMessage.messageText;
		return 
			areEqual(nextMessage.messageTypeString, "info") && messageText != null &&
			(messageText.startsWith("in expansion of") || areEqual(messageText, "expansion site"));
	}
	
	@Override
	protected ToolSourceMessage createMessage(ToolMessageData msgdata) throws CommonException {
		if(StringUtil.nullAsEmpty(msgdata.messageTypeString).isEmpty()) {
			msgdata.messageTypeString = "error";
		}
		return super.createMessage(msgdata);
	}
	
	@Override
	protected void addBuildMessage(ToolMessageData toolMessage_) throws CommonException {
		super.addBuildMessage(toolMessage_);
		
		if(toolMessage_ instanceof CompositeToolMessageData) {
			CompositeToolMessageData toolMessage = (CompositeToolMessageData) toolMessage_;
			if(toolMessage.nextMessage != null && areEqual(toolMessage.messageTypeString, "error")) {
				// This error message has linked errors - try to find expansion site error
				
				ArrayList2<CompositeToolMessageData> linkedErrors = new ArrayList2<>();
				buildChain(toolMessage, linkedErrors);
				
				Collections.reverse(linkedErrors);
				
				CompositeToolMessageData topError = linkedErrors.get(0);
				if(topError.messageText.equals("expansion site")) {
					
					super.addBuildMessage(createExpansionSiteErrorMessage(linkedErrors, topError));
				}
			}
		}
	}
	
	protected void buildChain(CompositeToolMessageData msg, ArrayList2<CompositeToolMessageData> errorsChain) {
		errorsChain.add(msg);
		
		if(msg.nextMessage == null) {
			return;
		}
		
		buildChain(msg.nextMessage, errorsChain);
	}
	
	protected ToolMessageData createExpansionSiteErrorMessage(ArrayList2<CompositeToolMessageData> linkedErrors, 
			CompositeToolMessageData topError) {
		
		// Note: it's ok to mutate topError, since it is otherwise no longer used
		topError.messageTypeString = "error";
		
		topError.messageText = "[macro expansion error]";
		
		for (int ix = 1; ix < linkedErrors.size(); ix++) {
			CompositeToolMessageData linkedError = linkedErrors.get(ix);
			topError.messageText += "\n" + linkedError.sourceBeforeMessageText + linkedError.simpleMessageText;
		}
		
		return topError;
	}

}