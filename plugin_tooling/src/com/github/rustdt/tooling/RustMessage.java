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

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.CoreUtil.areEqual;
import static melnorme.utilbox.core.CoreUtil.nullToEmpty;
import static melnorme.utilbox.misc.StringUtil.emptyAsNull;

import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.misc.HashcodeUtil;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.misc.ToStringHelper;
import melnorme.utilbox.status.Severity;

/**
 * A structured Rust message as emitted by the Rust compiler.
 */
public abstract class RustMessage implements ToStringHelper.ToString {
	
	protected final ToolSourceMessage sourceMessage;
	protected final boolean isPrimary;
	protected final Indexable<String> notes;
	protected final Indexable<RustMessage> spans;
	
	public RustMessage(ToolSourceMessage baseMessage, boolean isPrimary) {
		this(baseMessage, isPrimary, null, null);
	}
	
	public RustMessage(
		ToolSourceMessage sourceMessage, boolean isPrimary, Indexable<String> notes, Indexable<RustMessage> spans
	) {
		super();
		this.sourceMessage = assertNotNull(sourceMessage);
		this.isPrimary = isPrimary;
		this.notes = nullToEmpty(notes);
		this.spans = nullToEmpty(spans);
	}
	
	public static boolean equals(RustMessage obj, RustMessage other) {
		if(obj == null || other == null) {
			return obj == other;
		}
		return 
			areEqual(obj.sourceMessage, other.sourceMessage) &&
			areEqual(obj.isPrimary, other.isPrimary) &&
			areEqual(obj.notes, other.notes) &&
			areEqual(obj.spans, other.spans)
		;
	}
	
	@Override
	public int hashCode() {
		return HashcodeUtil.combinedHashCode(sourceMessage, isPrimary, spans);
	}
	
	
	@Override
	public String toString() {
		return defaultToString();
	}
	
	@Override
	public void toString(ToStringHelper sh) {
		sh.writeBlock("RUST_MESSAGE {", (sh2) -> {
			sh2.writeElementWithPrefix("MESSAGE:", sourceMessage);
			if(isPrimary){
				sh2.writeElement("IS_PRIMARY");
			}
			sh2.writeElementWithPrefix("NOTES:", notes);
			toStringSub(sh2);
			sh2.writeList("SPANS: [", spans, "]");
		},"}");
	}
	
	@SuppressWarnings("unused") 
	public void toStringSub(ToStringHelper sh) {
	}
	
	public String getMessageWithCode() {
		return sourceMessage.getMessage();
	}
	
	public String getMessageTextWithNotes() {
		String msgTextWithNotes = getMessageWithCode();
		
		if(!notes.isEmpty()) {
			msgTextWithNotes += ":";
		}
		for (String note : notes) {
			msgTextWithNotes += "\n" + note;
		}
		return msgTextWithNotes;
	}
	
	public void collectToolMessages(
		ArrayList2<ToolSourceMessage> sourceMessages, RustMainMessage mainMessage, boolean isExpansion
	) {
		ToolSourceMessage srcMessage = this.sourceMessage;
		Severity severity = srcMessage.severity;
		
		boolean hasOwnMessage = emptyAsNull(srcMessage.message) != null;
		String newText = null;
		
		if(isPrimary && mainMessage != null) {
			newText = mainMessage.getMessageTextWithNotes();
			if(mainMessage.notes.isEmpty() && hasOwnMessage) {
				newText = StringUtil.addSuffix(newText, ":");
			}
			mainMessage = null;
		}
		
		if(hasOwnMessage) {
			newText = StringUtil.joinUsingSep(newText, "\n", srcMessage.message, true);
		} else {
			if(isExpansion) {
				// if this message is a repetition (from macro), downgrade severity to info
				severity = Severity.INFO;
			}
		}
		
		newText = StringUtil.nullAsEmpty(newText);
		
		srcMessage = new ToolSourceMessage(srcMessage.path, srcMessage.range, severity, newText);
		sourceMessages.add(srcMessage);
	}
	
}