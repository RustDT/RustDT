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
package melnorme.lang.tooling;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertUnreachable;

@LANG_SPECIFIC
public enum CompletionProposalKind {
	
	UNKNOWN,
	KEYWORD,
	ERROR,
	
	// The rest, based upon https://github.com/phildawes/racer/blob/master/src/racer/mod.rs#L22
    Struct,
    Module,
    MatchArm,
    Function,
    Crate,
    Let,
    IfLet,
    StructField,
    Impl,
    Enum,
    EnumVariant,
    Type,
    FnArg,
    Trait,
    Const,
    Static;
	
	
	public <RET> RET switchOnKind(ProposalKindVisitor<RET> visitor) {
		switch(this) {
		case UNKNOWN: return visitor.visitUnknown();
		case KEYWORD: return visitor.visitKeyword();
		case ERROR: return visitor.visitError();
		
		case Let: return visitor.visitVariable();
		case IfLet: return visitor.visitVariable();
		case FnArg: return visitor.visitVariable();
		
		case Function: return visitor.visitFunction();
		
		case Struct: return visitor.visitStruct();
		case Type: return visitor.visitType();
		case StructField: return visitor.visitVariable();
		
		case Module: return visitor.visitModule();
		
		case Trait: return visitor.visitTrait();
		
		case Const: return visitor.visitConst();
		case Static: return visitor.visitStatic();
		case Crate: return visitor.visitOther();
		case Enum: return visitor.visitEnum();
		case EnumVariant: return visitor.visitEnumVariant();
		case Impl: return visitor.visitImpl();
		case MatchArm: return visitor.visitMatchArm();
		}
		throw assertUnreachable();
	}
	
	public static interface ProposalKindVisitor<RET> extends AbstractKindVisitor<RET> {
			
		RET visitError();
		
		public abstract RET visitType();
		
		public abstract RET visitTrait();
		
		public abstract RET visitConst();
		public abstract RET visitStatic();
		public abstract RET visitEnumVariant();
		public abstract RET visitImpl();
		public abstract RET visitMatchArm();
		public abstract RET visitOther();
		
		public abstract RET visitCrate();
		
	}
	
}