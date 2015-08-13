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
package com.github.rustdt.ide.core.cargomodel;

import org.eclipse.core.resources.IProject;

import com.github.rustdt.ide.core.cargomodel.RustBundleModelManager.RustBundleModel;
import com.github.rustdt.tooling.cargo.CargoManifestParser;

import melnorme.lang.ide.core.operations.build.BuildManager.BuildConfiguration;
import melnorme.lang.ide.core.project_model.AbstractBundleInfo;
import melnorme.lang.ide.core.project_model.BundleManifestResourceListener;
import melnorme.lang.ide.core.project_model.BundleModelManager;
import melnorme.lang.ide.core.project_model.LangBundleModel;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;

/**
 * In Rust, the bundles are the Cargo crates. 
 */
public class RustBundleModelManager extends BundleModelManager<AbstractBundleInfo, RustBundleModel> {
	
	public static class RustBundleModel extends LangBundleModel<AbstractBundleInfo> {
		
	}
	
	/* -----------------  ----------------- */
	
	public RustBundleModelManager() {
		super(new RustBundleModel());
	}
	
	/* -----------------  ----------------- */
	
	@Override
	protected BundleManifestResourceListener init_createResourceListener() {
		return new ManagerResourceListener(ResourceUtils.epath(CargoManifestParser.BUNDLE_MANIFEST_FILE));
	}
	
	@Override
	protected AbstractBundleInfo createNewInfo(IProject project) {
		return new AbstractBundleInfo() {
			
			@Override
			public Indexable<BuildConfiguration> getBuildConfigurations() {
				return ArrayList2.create(new BuildConfiguration("", null));
			}
			
		};
	}
	
}