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
package com.github.rustdt.ide.ui.actions;

import melnorme.lang.ide.ui.editor.LangEditorContextMenuContributor;

import org.eclipse.ui.services.IServiceLocator;

public class RustEditorContextMenuContributor extends LangEditorContextMenuContributor {
	
	public RustEditorContextMenuContributor(IServiceLocator svcLocator) {
		super(svcLocator);
	}
	
}