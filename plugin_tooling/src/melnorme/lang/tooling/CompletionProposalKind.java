/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.tooling;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertUnreachable;

@LANG_SPECIFIC
public enum CompletionProposalKind {
	
	KEYWORD,
	UNKNOWN,
	
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
	
	public static abstract class ProposalKindVisitor<RET> extends AbstractKindVisitor<RET> {
		
		public RET switchOnKind(CompletionProposalKind kind) {
			switch(kind) {
			case KEYWORD: return visitKeyword();
			case UNKNOWN: return visitUnknown();
			
			case Let: return visitVariable();
			case IfLet: return visitVariable();
			case FnArg: return visitVariable();
			
			case Function: return visitFunction();
			
			case Struct: return visitStruct();
			case Type: return visitType();
			case StructField: return visitStructField();
			
			case Module: return visitModule();
			
			case Trait: return visitTrait();
			
			case Const: return visitConst();
			case Static: return visitStatic();
			case Crate: return visitCrate();
			case Enum: return visitEnum();
			case EnumVariant: return visitEnumVariant();
			case Impl: return visitImpl();
			case MatchArm: return visitMatchArm();
			}
			throw assertUnreachable();
		}
		
		protected abstract RET visitType();
		
		protected abstract RET visitTrait();
		
		private RET visitStructField() {
			return visitVariable();
		}
		
		protected RET visitConst() {
			return visitVariable();
		}
		protected RET visitStatic() {
			return visitVariable();
		}
		protected RET visitCrate() {
			return visitOther();
		}
		protected RET visitEnumVariant() {
			return visitVariable();
		}
		protected RET visitImpl() {
			return visitOther();
		}
		protected RET visitMatchArm() {
			return visitOther();
		}
		protected abstract RET visitOther();
		
		/* ----------------- Not applicable: ----------------- */
		
		@Override
		protected final RET visitTemplate() {
			throw assertFail();
		}
		@Override
		protected final RET visitAlias() {
			throw assertFail();
		}
		
	}
	
}