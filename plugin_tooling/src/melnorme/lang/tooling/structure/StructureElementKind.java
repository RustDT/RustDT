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
package melnorme.lang.tooling.structure;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertUnreachable;
import melnorme.lang.tooling.AbstractKindVisitor;
import melnorme.lang.tooling.LANG_SPECIFIC;


@LANG_SPECIFIC
public enum StructureElementKind {
	
	VAR,
	FUNCTION,
	STRUCT,
	UNION, // Upcoming Rust feature
	IMPL,
	TRAIT,
	ENUM,
	ENUM_VARIANT,
	
	EXTERN_CRATE,
	MOD,
	USE,
	USE_GROUP,
	
	TYPE_ALIAS, 
	MACRO,
	
	UNKNOWN,
	;
	
	
	public <RET> RET switchOnKind(StructureElementKindVisitor<RET> visitor) {
		return switchOnKind(this, visitor);
	}
	
	public static <RET> RET switchOnKind(StructureElementKind kind, StructureElementKindVisitor<RET> visitor) {
		switch(kind) {
		case VAR: return visitor.visitVariable();
		
		case FUNCTION: return visitor.visitFunction();
		
		case STRUCT: return visitor.visitStruct();
		case UNION: return visitor.visitUnion();
		case IMPL: return visitor.visitImpl();
		case TRAIT: return visitor.visitTrait();
		case ENUM: return visitor.visitEnum();
		case ENUM_VARIANT: return visitor.visitEnumVariant();
		
		case EXTERN_CRATE: return visitor.visitExternCrate();
		case MOD: return visitor.visitModule();
		case USE: return visitor.visitUse();
		case USE_GROUP: return visitor.visitUseGroup();
		
		case TYPE_ALIAS: return visitor.visitTypeAlias();
		case MACRO: return visitor.visitMacro();
		case UNKNOWN: return visitor.visitUnknown();
		
		}
		throw assertUnreachable();
	}
	
	public static interface StructureElementKindVisitor<RET> extends AbstractKindVisitor<RET> {
		
		public abstract RET visitUnion();
		public abstract RET visitImpl();
		public abstract RET visitTrait();
		
		public abstract RET visitExternCrate();
		public abstract RET visitUse();
		public abstract RET visitUseGroup();
		
		public abstract RET visitEnumVariant();
		
		public abstract RET visitTypeAlias();
		public abstract RET visitMacro();
		
	}
	
}