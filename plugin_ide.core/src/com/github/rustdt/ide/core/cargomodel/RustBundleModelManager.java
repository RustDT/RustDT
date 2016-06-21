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

import static com.github.rustdt.tooling.cargo.CargoManifestParser.MANIFEST_FILENAME;

import org.eclipse.core.resources.IProject;

import com.github.rustdt.ide.core.cargomodel.RustBundleModelManager.RustBundleModel;
import com.github.rustdt.tooling.cargo.CargoManifest;
import com.github.rustdt.tooling.cargo.CargoManifestParser;

import melnorme.lang.ide.core.project_model.BundleManifestResourceListener;
import melnorme.lang.ide.core.project_model.BundleModelManager;
import melnorme.lang.ide.core.project_model.LangBundleModel;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.bundle.BundleInfo;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.FileUtil;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;

/**
 * In Rust, the bundles are the Cargo crates. 
 */
public class RustBundleModelManager extends BundleModelManager<RustBundleModel> {
	
	public static class RustBundleModel extends LangBundleModel {
		
	}
	
	/* -----------------  ----------------- */
	
	public RustBundleModelManager() {
		super(new RustBundleModel());
	}
	
	/* -----------------  ----------------- */
	
	@Override
	protected BundleManifestResourceListener init_createResourceListener() {
		return new ManagerResourceListener();
	}
	
	@Override
	protected BundleInfo createNewInfo(IProject project) {
		String manifestSource;
		try {
			Location loc = ResourceUtils.getProjectLocation2(project).resolve(MANIFEST_FILENAME);
			manifestSource = FileUtil.readFileContents(loc, StringUtil.UTF8);
		} catch(CommonException e) {
			return new BundleInfo(new CargoManifest("<cargo.toml read error>", null, null, null, null, null));
		}
		
		try {
			CargoManifest manifest = new CargoManifestParser().parse(manifestSource);
			return new BundleInfo(manifest);
		} catch(CommonException e) {
			return new BundleInfo(new CargoManifest("<cargo.toml parse error>", null, null, null, null, null));
		}
		
		// TODO: we could have a better mechanism for error reporting: actually put the exception message somewhere
	}
	
}