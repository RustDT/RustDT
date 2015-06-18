/*******************************************************************************
 * Copyright (c) 2014, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.navigator;

import java.text.Collator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.ViewerSorter;

public class RustNavigatorSorter extends ViewerSorter {
	
	public RustNavigatorSorter() {
		super();
	}
	
	public RustNavigatorSorter(Collator collator) {
		super(collator);
	}
	
	@Override
	public int category(Object element) {
		if(element instanceof IFolder) {
			return -2;
		} 
		return 0;
	}
	
}