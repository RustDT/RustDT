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

import java.text.MessageFormat;

import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.misc.HashcodeUtil;
import melnorme.utilbox.misc.StringUtil;

public class CargoMessage {
	
	public static class CargoMessageTarget {
		
		protected final CargoMessageTargetData messageTargetData;
		
		public CargoMessageTarget(Indexable<String> kind, String name, String path) {
			this(new CargoMessageTargetData(kind, name, path));
		}
		
		public CargoMessageTarget(CargoMessageTargetData cargoMessageTargetData) {
			this.messageTargetData = assertNotNull(cargoMessageTargetData);
			assertNotNull(cargoMessageTargetData.kind); 
			assertNotNull(cargoMessageTargetData.name);
			assertNotNull(cargoMessageTargetData.path);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof CargoMessageTarget)) return false;
			
			CargoMessageTarget other = (CargoMessageTarget) obj;
			
			return areEqual(messageTargetData, other.messageTargetData);
		}
		
		@Override
		public int hashCode() {
			return HashcodeUtil.combinedHashCode(messageTargetData);
		}
		
		@Override
		public String toString() {
			return messageTargetData.toString();
		}
		
	}
	
	public static class CargoMessageTargetData {
		
		public Indexable<String> kind;
		public String name;
		public String path;
		
		public CargoMessageTargetData(Indexable<String> kind, String name, String path) {
			this.kind = kind;
			this.name = name;
			this.path = path;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof CargoMessageTargetData)) return false;
			
			CargoMessageTargetData other = (CargoMessageTargetData) obj;
			
			return 
				areEqual(kind, other.kind) && 
				areEqual(name, other.name) &&
				areEqual(path, other.path)
			;
		}
		
		@Override
		public int hashCode() {
			return HashcodeUtil.combinedHashCode(kind, name, path);
		}
		
		@Override
		public String toString() {
			return MessageFormat.format(
				"[ kind: {0}, name: {1}, path: {2} ]", 
				StringUtil.collToString(this.kind, ","),
				this.name,
				this.path
			);
		}
		
	}
	
	/* -----------------  ----------------- */
	
	protected final String reason;
	protected final String packageId;
	protected final CargoMessageTarget target;
	protected final RustMainMessage message;
	
	public CargoMessage(String reason, String packageId, CargoMessageTarget target, RustMainMessage message) {
		super();
		this.reason = StringUtil.nullAsEmpty(reason);
		this.packageId = assertNotNull(packageId);
		this.target = assertNotNull(target);
		this.message = assertNotNull(message);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof CargoMessage)) return false;
		
		CargoMessage other = (CargoMessage) obj;
		
		return 
			areEqual(reason, other.reason) &&
			areEqual(packageId, other.packageId) &&
			areEqual(target, other.target) &&
			areEqual(message, other.message)
		;
	}
	
	@Override
	public int hashCode() {
		return HashcodeUtil.combinedHashCode(reason, packageId, target, message);
	}
	
	@Override
	public String toString() {
		return MessageFormat.format(
			"[ REASON: {0}, PACKAGE_ID: {1}, TARGET: {2}, RUST_MESSAGE... ]", 
			this.reason,
			this.packageId,
			this.target
		);
	}
	
}