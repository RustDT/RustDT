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

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.CoreUtil.areEqual;

import java.util.Collections;

import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.lang.tooling.toolchain.ops.BuildOutputParser2;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.tooling.toolchain.ops.ToolMessageData;
import melnorme.lang.utils.parse.LexingUtils;
import melnorme.lang.utils.parse.StringCharSource;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.HashcodeUtil;
import melnorme.utilbox.misc.IByteSequence;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Class output parser. 
 * This has been made obsolete with new error format in Rust 1.12, 
 * so it will be eventually phased out.
 */
public abstract class RustBuildOutputParser extends BuildOutputParser2 {
	
	@Override
	protected void handleNonZeroExitCode(ExternalProcessResult result) throws CommonException, OperationSoftFailure {
		// Do nothing
	}
	
	@Override
	protected IByteSequence getOutputFromProcessResult(ExternalProcessResult result) {
		return result.getStdErrBytes();
	}
	
	protected boolean isMessageEnd(String nextLine) {
		return nextLine.startsWith("error: aborting due to ");
	}
	
	@Override
	protected CompositeToolMessageData parseMessageData(StringCharSource output) throws CommonException {
		
		String outputLine = LexingUtils.consumeLine(output);
		
		if(isMessageEnd(outputLine)) {
			// We reached the end of messages, exhaust remaining tool output.
			while(output.consumeAny()) {
			}
			return null;
		}
		
		ToolMessageData messageData = RUSTC_OUTPUT_LINE_PARSER.parseLine(outputLine);
		if(messageData == null) {
			ToolMessageData cargoMessageData = CARGO_OUTPUT_LINE_PARSER.parseLine(outputLine);
			if (cargoMessageData != null) {
				return new CompositeToolMessageData(cargoMessageData);
			} else {
				throw createUnknownLineSyntaxError(outputLine);
			}
		}
		
		CompositeToolMessageData compositeMessageData = new CompositeToolMessageData(messageData);
		
		parseMultiLineMessageText(output, compositeMessageData);
		
		parseAssociatedMessage(output, compositeMessageData);
		
		return compositeMessageData;
	}
	
	protected static final AbstractRustBuildOutputLineParser CARGO_OUTPUT_LINE_PARSER = 
		new CargoRustBuildOutputLineParser();
	protected static final AbstractRustBuildOutputLineParser RUSTC_OUTPUT_LINE_PARSER =
		new ClassicRustcRustBuildOutputLineParser();
		
	protected void parseMultiLineMessageText(StringCharSource output, CompositeToolMessageData msg) {
		while(true) {
			String lineAhead = LexingUtils.stringUntilNewline(output);
			
			if(lineAhead.isEmpty() || RUSTC_OUTPUT_LINE_PARSER.canParseLine(lineAhead)) {
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
				// don't add this message, or nextLine, to actual error message.
				break;
			}
			
			msg.messageText += "\n" + lineAhead;
		}
	}
	
	protected void parseAssociatedMessage(StringCharSource output, CompositeToolMessageData msg)
			throws CommonException {
		String lineAhead = LexingUtils.stringUntilNewline(output);
		
		msg.simpleMessageText = msg.messageText;
		
		ToolMessageData nextMessage = RUSTC_OUTPUT_LINE_PARSER.parseLine(lineAhead);
		if(nextMessage != null) {
			
			if(isTemplateInstantiationMessage(nextMessage)) {
				CompositeToolMessageData compositeNextMessage = parseMessageData(output);
				msg.nextMessage = compositeNextMessage;
				msg.messageText += "\n" + compositeNextMessage.getFullMessageSource();
			}
			
		}
	}
	
	public static class CompositeToolMessageData extends ToolMessageData {
		
		public CompositeToolMessageData nextMessage;
		public String simpleMessageText;
		
		public CompositeToolMessageData() {
			
		}
		
		public CompositeToolMessageData(ToolMessageData init) {
			super(init);
		}
		
		public String getFullMessageSource() {
			assertNotNull(sourceBeforeMessageText);
			assertNotNull(messageText);
			
			return sourceBeforeMessageText + messageText;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof CompositeToolMessageData)) return false;
			
			CompositeToolMessageData other = (CompositeToolMessageData) obj;
			
			return 
				equalsOther(other) &&
				areEqual(nextMessage, other.nextMessage) &&
				areEqual(simpleMessageText, other.simpleMessageText)
			;
		}
		
		@Override
		public int hashCode() {
			return HashcodeUtil.combinedHashCode(super.hashCode(), nextMessage);
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
	
	public ToolSourceMessage createMessage(ToolMessageData msgdata) throws CommonException {
		if(StringUtil.nullAsEmpty(msgdata.messageTypeString).isEmpty()) {
			msgdata.messageTypeString = "error";
		}
		return toolMessageParser.createMessage(msgdata);
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