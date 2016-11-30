/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling;

import static melnorme.utilbox.core.CoreUtil.areEqual;

import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.misc.HashcodeUtil;
import melnorme.utilbox.misc.StringUtil;

public class RustMainMessage extends RustMessage {
	
	protected final String errorCode; // can be null 
	
	public RustMainMessage(
		ToolSourceMessage baseMessage, String errorCode, Indexable<String> notes, Indexable<RustMessage> spans
	) {
		super(baseMessage, true, notes, spans);
		this.errorCode = errorCode;
	}
	
	@Override
	public final boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof RustMainMessage)) return false;
		
		RustMainMessage other = (RustMainMessage) obj;
		
		return 
			areEqual(errorCode, other.errorCode) &&
			RustMessage.equals(this, other)
		;
	}
	
	@Override
	public final int hashCode() {
		return HashcodeUtil.combinedHashCode(errorCode, super.hashCode());
	}
	
	@Override
	public String getMessageWithCode() {
		String msgTextWithNotes = sourceMessage.getMessage();
		if(errorCode != null) {
			msgTextWithNotes += StringUtil.mapSurround(" [", errorCode, "]");
		}
		return msgTextWithNotes;
	}
	
	public ArrayList2<ToolSourceMessage> retrieveToolMessages() {
		ArrayList2<ToolSourceMessage> sourceMessages = new ArrayList2<>();
		
		if(spans.isEmpty()) {
			ToolSourceMessage tsm = this.sourceMessage;
			// Message without range (TODO: add tests, but need to recreate in Cargo/rustc first
			sourceMessages.add(
				new ToolSourceMessage(tsm.path, tsm.range, tsm.severity, tsm.message)
			);			
		} else {
			for (RustMessage spanMessage : spans) {
				spanMessage.collectToolMessages(sourceMessages, this);
			}
		}
		
		return sourceMessages;
	}
	
}