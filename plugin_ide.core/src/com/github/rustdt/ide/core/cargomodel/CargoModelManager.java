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
package com.github.rustdt.ide.core.cargomodel;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import com.github.rustdt.ide.core.cargomodel.CargoWorkspaceModel.ProjectInfo;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.project_model.BundleManifestResourceListener;
import melnorme.lang.ide.core.project_model.BundleModelManager;
import melnorme.lang.tooling.IBundleInfo;
import melnorme.utilbox.misc.SimpleLogger;

public class CargoModelManager extends BundleModelManager {
	
	public static final Path BUNDLE_MANIFEST_FILE = new Path("Cargo.toml");
	
	/* -----------------  ----------------- */
	
	protected final CargoWorkspaceModel model = new CargoWorkspaceModel();
	
	@Override
	protected BundleManifestResourceListener init_createResourceListener() {
		return new ManagerResourceListener(BUNDLE_MANIFEST_FILE);
	}
	
	@Override
	protected IBundleInfo getProjectInfo(IProject project) {
		return model.getProjectInfo(project);
	}
	
	@Override
	protected void bundleProjectAdded(final IProject project) {
		model.setProjectInfo(project, new ProjectInfo());
		
	}
	
	@Override
	protected void bundleProjectRemoved(final IProject project) {
		if(model.getProjectInfo(project) != null) {
			model.removeProjectInfo(project);
		} else {
			LangCore.logWarning("Unexpected: model.getProjectInfo(project) != null");
		}
		
	}
	
	@Override
	protected void bundleManifestFileChanged(IProject project) {
		bundleProjectAdded(project);
	}
	
}

class CargoWorkspaceModel extends Object /*implements IWorkspaceModel*/ {
	
	protected final SimpleLogger log = CargoModelManager.log;
	
	protected final HashMap<String, ProjectInfo> projectInfos = new HashMap<>();
	
	public CargoWorkspaceModel() {
	}
	
//	@Override
	public synchronized ProjectInfo getProjectInfo(IProject project) {
		return projectInfos.get(project.getName());
	}
	
//	@Override
	public synchronized Set<String> getDubProjects() {
		return new HashSet<>(projectInfos.keySet());
	}
	
	protected synchronized ProjectInfo setProjectInfo(IProject project, ProjectInfo newProjectInfo) {
		String projectName = project.getName();
		projectInfos.put(projectName, newProjectInfo);
		log.println("Cargo project model added: " + projectName);
//		fireUpdateEvent(new DubModelUpdateEvent(project, newProjectInfo.getBundleDesc()));
		return newProjectInfo;
	}
	
	protected synchronized void removeProjectInfo(IProject project) {
		ProjectInfo oldProjectInfo = projectInfos.remove(project.getName());
		assertNotNull(oldProjectInfo);
//		DubBundleDescription oldDesc = oldProjectInfo.getBundleDesc();
		log.println("Cargo project model removed: " + project.getName());
//		fireUpdateEvent(new DubModelUpdateEvent(project, oldDesc));
	}
	
	public static class ProjectInfo implements IBundleInfo {
		// TODO: no info at the moment.
	}
	
}