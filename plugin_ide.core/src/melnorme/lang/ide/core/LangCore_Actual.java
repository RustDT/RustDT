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
package melnorme.lang.ide.core;

import com.github.rustdt.ide.core.cargomodel.RustBundleModelManager;
import com.github.rustdt.ide.core.cargomodel.RustBundleModelManager.RustBundleModel;
import com.github.rustdt.ide.core.engine.RustEngineClient;
import com.github.rustdt.ide.core.operations.RustBuildManager;
import com.github.rustdt.ide.core.operations.RustToolManager;

import melnorme.lang.ide.core.engine.EngineClient;
import melnorme.lang.ide.core.operations.AbstractToolManager;
import melnorme.lang.ide.core.operations.build.BuildManager;

public class LangCore_Actual {
	
	public static final String PLUGIN_ID = "com.github.rustdt.ide.core";
	public static final String NATURE_ID = PLUGIN_ID +".nature";
	
	public static final String BUILDER_ID = PLUGIN_ID + ".Builder";
	public static final String BUILD_PROBLEM_ID = PLUGIN_ID + ".build_problem";
	public static final String SOURCE_PROBLEM_ID = PLUGIN_ID + ".source_problem";
	
	public static final String LANGUAGE_NAME = "Rust";
	
	public static AbstractToolManager createToolManagerSingleton() {
		return new RustToolManager();
	}
	
	public static EngineClient createEngineClient() {
		return new RustEngineClient();
	}
	
	public static RustBundleModelManager createBundleModelManager() {
		return new RustBundleModelManager();
	}
	public static RustBundleModel getBundleModel() {
		return (RustBundleModel) LangCore.getBundleModel();
	}
	public static BuildManager createBuildManager() {
		return new RustBuildManager(getBundleModel());
	}
	
}