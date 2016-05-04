/*******************************************************************************
 * Copyright (c) 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.wizards;

import com.github.rustdt.ide.ui.wizards.RustProjectWizard;

import melnorme.lang.ide.ui.dialogs.LangProjectWizardTest;


public class RustProjectWizardTest extends LangProjectWizardTest {
	
	@Override
	protected RustProjectWizard createNewProjectWizard() {
		RustProjectWizard rustProjectWizard = new RustProjectWizard();
		rustProjectWizard.firstPage.useCargoInit.set(false);
		return rustProjectWizard;
	}
	
}