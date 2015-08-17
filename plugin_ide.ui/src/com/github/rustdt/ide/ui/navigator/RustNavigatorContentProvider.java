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
package com.github.rustdt.ide.ui.navigator;

import org.eclipse.core.resources.IProject;

import com.github.rustdt.ide.core.cargomodel.RustBundleInfo;

import melnorme.lang.ide.core.LangCore_Actual;
import melnorme.lang.ide.core.project_model.view.DependenciesContainer;
import melnorme.lang.ide.ui.views.AbstractNavigatorContentProvider;
import melnorme.utilbox.collections.ArrayList2;

public class RustNavigatorContentProvider extends AbstractNavigatorContentProvider {
	
	@Override
	protected LangNavigatorSwitcher_HasChildren hasChildren_switcher() {
		return new LangNavigatorSwitcher_HasChildren() {
		};
	}
	
	@Override
	protected LangNavigatorSwitcher_GetChildren getChildren_switcher() {
		return new LangNavigatorSwitcher_GetChildren() {
			@Override
			public void addFirstProjectChildren(IProject project, ArrayList2<Object> projectChildren) {
				RustBundleInfo projectInfo = LangCore_Actual.getBundleModel().getProjectInfo(project);
				if(projectInfo != null) {
					projectChildren.add(new DependenciesContainer(projectInfo, project));
				}
			}
		};
	}
	
	@Override
	protected LangNavigatorSwitcher_GetParent getParent_switcher() {
		return new LangNavigatorSwitcher_GetParent() {
		};
	}
	
}