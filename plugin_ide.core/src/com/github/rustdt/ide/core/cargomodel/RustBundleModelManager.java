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

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;

import com.github.rustdt.ide.core.cargomodel.RustBundleModelManager.RustBundleModel;
import com.github.rustdt.tooling.cargo.CargoManifestParser;
import com.github.rustdt.tooling.cargo.CargoManifest;

import melnorme.lang.ide.core.BundleInfo;
import melnorme.lang.ide.core.project_model.BundleManifestResourceListener;
import melnorme.lang.ide.core.project_model.BundleModelManager;
import melnorme.lang.ide.core.project_model.LangBundleModel;
import melnorme.lang.ide.core.utils.ResourceUtils;
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
		return new ManagerResourceListener(ResourceUtils.epath(MANIFEST_FILENAME));
	}
	
	@Override
	protected BundleInfo createNewInfo(IProject project) {
		try {
			Location loc = ResourceUtils.getProjectLocation2(project).resolve(MANIFEST_FILENAME);
			
			String manifestSource = FileUtil.readStringFromFile(loc, StringUtil.UTF8, 
				() -> MessageFormat.format("Could not read `{0}` file: ", MANIFEST_FILENAME));
			
			CargoManifest manifest = new CargoManifestParser().parse(manifestSource);
			return new BundleInfo(manifest);
			
		} catch(CommonException e) {
			return new BundleInfo(new CargoManifest("<cargo.toml error>", null, null, null, null));
		}
		
	}
	
}