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


import static melnorme.utilbox.core.CoreUtil.list;
import static melnorme.utilbox.misc.MiscUtil.getClassResource;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import com.github.rustdt.tooling.cargo.CargoManifestParser;
import com.github.rustdt.tooling.cargo.RustNamingRules;

import melnorme.lang.ide.core.operations.RunToolOperation.RunSDKToolOperation;
import melnorme.lang.ide.core.utils.EclipseUtils;
import melnorme.lang.ide.ui.WizardMessages_Actual;
import melnorme.lang.ide.ui.dialogs.LangNewProjectWizard;
import melnorme.lang.ide.ui.dialogs.LangProjectWizardFirstPage;
import melnorme.util.swt.components.fields.CheckBoxField;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.status.Severity;
import melnorme.utilbox.status.StatusException;

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
			throws CommonException, OperationCancellation, CoreException {

		if(firstPage.useCargoInit.getBooleanFieldValue()) {
			new RunSDKToolOperation(getProject(), list("init")).execute(EclipseUtils.om(monitor));
		} else {
			configureHelloWorld(projectCreator, monitor);
		}
	}
	
	protected void configureHelloWorld(ProjectCreator_ForWizard projectCreator, IProgressMonitor monitor)
			throws CoreException {
		String cargoManifest = HelloWorld_ManifestContents.replaceAll(Pattern.quote("$project_name$"), 
			getProject().getName());
		
		projectCreator.createFile(getProject().getFile(CargoManifestParser.MANIFEST_FILENAME.toString()), 
			cargoManifest, false, monitor);
		
		IFile mainModule = getProject().getFolder("src").getFile("main.rs");
		projectCreator.createFile(mainModule, HelloWorld_ModuleContents, true, monitor);
	}
	
}

class RustProjectWizardFirstPage extends LangProjectWizardFirstPage {
	
	protected final CheckBoxField useCargoInit = new CheckBoxField("Use `cargo init` to create project.");
	
	public RustProjectWizardFirstPage() {
		super();
		setTitle(WizardMessages_Actual.LangNewProject_Page1_pageTitle);
		setDescription(WizardMessages_Actual.LangNewProject_Page1_pageDescription);
		
		useCargoInit.set(true);
		useCargoInit.addChangeListener(() -> nameGroup.nameField().fireFieldValueChanged());
	}
	
	@Override
	protected NameGroup createNameGroup() {
		return new NameGroup() {
			
			@Override
			public void validateProjectName() throws StatusException {
				super.validateProjectName();
				
				try {
					RustNamingRules.validateCrateName(getName());
				} catch(StatusException e) {
					if(useCargoInit.getBooleanFieldValue()) {
						throw e;
					} else {
						// Change validation to Warning
						throw new StatusException(Severity.WARNING, 
							e.getMessage() + 
							" The crate name will need to changed if the project is created with this name.");
					}
				}
			}

		};
	}
	
	@Override
	protected void createContents_ValidationGroups(Composite parent) {
		useCargoInit.createComponent(parent, sectionGDF().create());
		
		super.createContents_ValidationGroups(parent);
	}
	
}