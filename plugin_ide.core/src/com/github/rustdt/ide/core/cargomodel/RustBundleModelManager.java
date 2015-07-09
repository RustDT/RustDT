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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import com.github.rustdt.ide.core.cargomodel.RustBundleModelManager.RustBundleModel;

import melnorme.lang.ide.core.project_model.AbstractBundleInfo;
import melnorme.lang.ide.core.project_model.BundleManifestResourceListener;
import melnorme.lang.ide.core.project_model.BundleModelManager;
import melnorme.lang.ide.core.project_model.LangBundleModel;
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
	
	public static final Path BUNDLE_MANIFEST_FILE = new Path("Cargo.toml");
	
	/* -----------------  ----------------- */
	
	@Override
	protected BundleManifestResourceListener init_createResourceListener() {
		return new ManagerResourceListener(BUNDLE_MANIFEST_FILE);
	}
	
	@Override
	protected AbstractBundleInfo createNewInfo(IProject project) {
		return new AbstractBundleInfo() {
			
			protected final ArrayList2<BuildConfiguration> DEFAULT_BUILD_CONFIGs = ArrayList2.create(
				new BuildConfiguration(null, null)
			);
			
			@Override
			public java.nio.file.Path getEffectiveTargetFullPath() {
				return null;
			}
			
			@Override
			public Indexable<BuildConfiguration> getBuildConfigurations() {
				return DEFAULT_BUILD_CONFIGs;
			}
			
		};
	}
	
}