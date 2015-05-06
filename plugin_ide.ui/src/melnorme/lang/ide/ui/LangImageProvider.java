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

import com.github.rustdt.ide.ui.RustObjImages;

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
	public final IManagedImage visitTemplate() {
		throw assertFail();
	}
	@Override
	public final IManagedImage visitAlias() {
		throw assertFail();
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
	public IManagedImage visitEnumVariant() {
		return visitVariable();
	}
	@Override
	public IManagedImage visitImpl() {
		return visitOther();
	}
	@Override
	public IManagedImage visitMatchArm() {
		return visitOther();
	}
	
	@Override
	public IManagedImage visitType() {
		// This is an alias
		return RustObjImages.T_TYPE;
	}
	
	@Override
	public IManagedImage visitTrait() {
		return RustObjImages.T_TRAIT;
	}
	
	@Override
	public IManagedImage visitModule() {
		return LangElementImages.PACKAGE;
	}
	
	@Override
	public IManagedImage visitCrate() {
		return visitOther();
	}
	
	@Override
	public IManagedImage visitOther() {
		return new IManagedImage.NullManagedImage();
	}
	
}