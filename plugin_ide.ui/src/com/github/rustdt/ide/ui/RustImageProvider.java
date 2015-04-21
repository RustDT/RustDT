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
package com.github.rustdt.ide.ui;

import melnorme.lang.ide.ui.LangObjImages;
import melnorme.lang.ide.ui.views.LangImageProvider;

import org.eclipse.jface.resource.ImageDescriptor;


public class RustImageProvider extends LangImageProvider {
	
	@Override
	protected ImageDescriptor visitConst() {
		return super.visitConst();
	}
	
	@Override
	protected ImageDescriptor visitStatic() {
		return super.visitStatic();
	}
	
	@Override
	protected ImageDescriptor visitType() {
		// This is an alias
		return RustObjImages.T_TYPE.getDescriptor();
	}
	
	@Override
	protected ImageDescriptor visitTrait() {
		return RustObjImages.T_TRAIT.getDescriptor();
	}
	
	@Override
	protected ImageDescriptor visitModule() {
		return LangObjImages.PACKAGE.getDescriptor();
	}
	
	@Override
	protected ImageDescriptor visitCrate() {
		return super.visitCrate();
	}
	
	@Override
	protected ImageDescriptor visitOther() {
		return null;
	}
	
}