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

import melnorme.lang.tooling.common.ToolSourceMessage;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.misc.HashcodeUtil;
import melnorme.utilbox.misc.ToStringHelper;

public class RustSubMessage extends RustMessage {
	
	protected final RustMessage expansionMsg; // can be null
	protected final RustMessage defSiteMsg;  // can be null
	
	public RustSubMessage(ToolSourceMessage baseMessage) {
		this(baseMessage, true);
	}
	
	public RustSubMessage(ToolSourceMessage baseMessage, boolean isPrimary) {
		this(baseMessage, isPrimary, null, null);
	}
	
	public RustSubMessage(
		ToolSourceMessage baseMessage, boolean isPrimary, RustMessage expansionMsg, RustMessage defSiteMsg
	) {
		super(baseMessage, isPrimary);
		this.expansionMsg = expansionMsg;
		this.defSiteMsg = defSiteMsg;
	}

	@Override
	public final boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof RustSubMessage)) return false;
		
		RustSubMessage other = (RustSubMessage) obj;
		
		return 
			RustMessage.equals(this, other) &&
			RustMessage.equals(this.expansionMsg, other.expansionMsg) &&
			RustMessage.equals(this.defSiteMsg, other.defSiteMsg)
		;
	}
	
	@Override
	public int hashCode() {
		return HashcodeUtil.combinedHashCode(super.hashCode(), expansionMsg, defSiteMsg);
	}
	
	@Override
	public void toStringSub(ToStringHelper sh) {
		sh.writeElementWithPrefix("EXPANSION_SPAN:", expansionMsg, true);
		sh.writeElementWithPrefix("EXPANSION_DEF_SITE:", defSiteMsg, true);
	}
	
	
	@Override
	public void collectToolMessages(ArrayList2<ToolSourceMessage> sourceMessages, RustMainMessage mainMessage) {
		super.collectToolMessages(sourceMessages, mainMessage);
		
		if(expansionMsg != null) {
			expansionMsg.collectToolMessages(sourceMessages, mainMessage);
		}
		if(defSiteMsg != null) {
			defSiteMsg.collectToolMessages(sourceMessages, mainMessage);
		}
	}
}