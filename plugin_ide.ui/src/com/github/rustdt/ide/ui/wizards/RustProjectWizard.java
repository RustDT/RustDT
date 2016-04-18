/*******************************************************************************
 * Copyright (c) 2013 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.wizards;


import static melnorme.utilbox.misc.MiscUtil.getClassResource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.WizardPage;

import com.github.rustdt.tooling.cargo.CargoManifestParser;

import melnorme.lang.ide.ui.WizardMessages_Actual;
import melnorme.lang.ide.ui.dialogs.LangNewProjectWizard;
import melnorme.lang.ide.ui.dialogs.LangProjectWizardFirstPage;

/**
 * Rust New Project Wizard.
 */
public class RustProjectWizard extends LangNewProjectWizard {
	
	protected final RustProjectWizardFirstPage firstPage = new RustProjectWizardFirstPage();
	
	@Override
	public LangProjectWizardFirstPage getFirstPage() {
		return firstPage;
	}
	
	@Override
	public WizardPage getSecondPage() {
		return null;
	}
	
	@Override
	public void addPages() {
		addPage(firstPage);
	}
	
	protected static final String HelloWorld_ManifestContents = getClassResource(
		RustProjectWizard.class, "hello_world.Cargo.toml");
	protected static final String HelloWorld_ModuleContents = getClassResource(
		RustProjectWizard.class, "hello_world.rs");
	
	@Override
	protected void configureCreatedProject(ProjectCreator_ForWizard projectCreator, IProgressMonitor monitor)
			throws CoreException {
		projectCreator.createFile(getProject().getFile(CargoManifestParser.MANIFEST_FILENAME.toString()), 
			HelloWorld_ManifestContents, false, monitor);
		
		IFile mainModule = getProject().getFolder("src").getFile("main.rs");
		projectCreator.createFile(mainModule, HelloWorld_ModuleContents, true, monitor);
	}
	
}

class RustProjectWizardFirstPage extends LangProjectWizardFirstPage {
	
	public RustProjectWizardFirstPage() {
		setTitle(WizardMessages_Actual.LangNewProject_Page1_pageTitle);
		setDescription(WizardMessages_Actual.LangNewProject_Page1_pageDescription);
	}
	
}