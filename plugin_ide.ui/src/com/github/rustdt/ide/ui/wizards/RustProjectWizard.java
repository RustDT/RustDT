/*******************************************************************************
 * Copyright (c) 2013, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.wizards;


import static melnorme.utilbox.misc.MiscUtil.getClassResourceAsString;
import melnorme.lang.ide.ui.WizardMessages_Actual;
import melnorme.lang.ide.ui.dialogs.LangNewProjectWizard;
import melnorme.lang.ide.ui.dialogs.LangProjectWizardFirstPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.WizardPage;

import com.github.rustdt.ide.core.cargomodel.CargoModelManager;

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
	
	@Override
	protected ProjectCreator_ForWizard createProjectCreator() {
		return new RustProjectCreator();
	}
	
	protected static final String HelloWorld_DubJsonTemplate = getClassResourceAsString(
		RustProjectWizard.class, "hello_world.Cargo.toml");
	protected static final String HelloWorld_ModuleContents = getClassResourceAsString(
		RustProjectWizard.class, "hello_world.rs");
	
	public class RustProjectCreator extends ProjectCreator_ForWizard {
		
		public RustProjectCreator() {
			super(RustProjectWizard.this);
		}
		
		@Override
		protected void configureCreatedProject(IProgressMonitor monitor) throws CoreException {
			createSampleHelloWorldBundle(CargoModelManager.BUNDLE_MANIFEST_FILE.toOSString(), "src", "main.rs");
		}
		
		@Override
		protected String getDefaultManifestFileContents() {
			return HelloWorld_DubJsonTemplate;
		}
		
		@Override
		protected String getHelloWorldContents() {
			return HelloWorld_ModuleContents;
		}
		
	}
	
}

class RustProjectWizardFirstPage extends LangProjectWizardFirstPage {
	
	public RustProjectWizardFirstPage() {
		setTitle(WizardMessages_Actual.LangNewProject_Page1_pageTitle);
		setDescription(WizardMessages_Actual.LangNewProject_Page1_pageDescription);
	}
	
}