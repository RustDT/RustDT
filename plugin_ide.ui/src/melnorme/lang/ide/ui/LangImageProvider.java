/*******************************************************************************
 * Copyright (c) 2015, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.ide.ui;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertUnreachable;

import com.github.rustdt.ide.ui.RustElementImages;

import melnorme.lang.ide.ui.views.AbstractLangImageProvider;
import melnorme.lang.tooling.LANG_SPECIFIC;
import melnorme.util.swt.jface.IManagedImage;

@LANG_SPECIFIC
public class LangImageProvider extends AbstractLangImageProvider {
	
	@Override
	public IManagedImage visitKeyword() {
		throw assertUnreachable();
	}
	
	@Override
	public IManagedImage visitConst() {
		return visitVariable();
	}
	@Override
	public IManagedImage visitStatic() {
		return visitVariable();
	}
	
	@Override
	public IManagedImage visitVariable() {
		return super.visitVariable();
	}
	
	@Override
	public IManagedImage visitFunction() {
		return super.visitFunction();
	}
	
	@Override
	public IManagedImage visitStruct() {
		return super.visitStruct();
	}
	
	@Override
	public IManagedImage visitUnion() {
		return LangElementImages.T_UNION;
	}
	@Override
	public IManagedImage visitImpl() {
		return RustElementImages.T_IMPL;
	}
	
	@Override
	public IManagedImage visitTrait() {
		return RustElementImages.T_TRAIT;
	}
	
	@Override
	public IManagedImage visitEnum() {
		return super.visitEnum();
	}
	@Override
	public IManagedImage visitEnumVariant() {
		return RustElementImages.T_ENUM_VARIANT;
	}
	
	@Override
	public IManagedImage visitExternCrate() {
		return RustElementImages.PACKAGE;
	}
	
	@Override
	public IManagedImage visitModule() {
//		return LangElementImages.PACKAGE;
		return super.visitModule();
	}
	
	@Override
	public IManagedImage visitUse() {
		return RustElementImages.IMPORT;
	}
	
	@Override
	public IManagedImage visitUseGroup() {
		return RustElementImages.IMPORTS;
	}
	
	@Override
	public IManagedImage visitTypeAlias() {
		return RustElementImages.T_TYPE;
	}
	
	@Override
	public IManagedImage visitAlias() {
		throw assertFail();
	}
	
	@Override
	public IManagedImage visitMacro() {
		return RustElementImages.T_MACRO;
	}
	
	/* -----------------  ----------------- */
	
	@Override
	public IManagedImage visitMatchArm() {
		return visitOther();
	}
	
	@Override
	public IManagedImage visitType() {
		// This is an alias
		return visitTypeAlias();
	}
	
	
	@Override
	public IManagedImage visitCrate() {
		return visitExternCrate();
	}
	
	@Override
	public IManagedImage visitOther() {
		return new IManagedImage.NullManagedImage();
	}
	
}