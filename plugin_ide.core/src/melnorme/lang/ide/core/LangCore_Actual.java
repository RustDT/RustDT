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
package melnorme.lang.ide.core;

import com.github.rustdt.ide.core.cargomodel.RustBundleModelManager;
import com.github.rustdt.ide.core.engine.RustLanguageServerHandler;
import com.github.rustdt.ide.core.engine.RustSourceModelManager;
import com.github.rustdt.ide.core.operations.RustBuildManager;
import com.github.rustdt.ide.core.operations.RustToolManager;
import com.github.rustdt.tooling.ops.RustSDKLocationValidator;

import melnorme.lang.ide.core.operations.ToolManager;
import melnorme.utilbox.misc.ILogHandler;

public class LangCore_Actual extends AbstractLangCore {
	
	public static final String PLUGIN_ID = "com.github.rustdt.ide.core";
	public static final String NATURE_ID = PLUGIN_ID +".nature";
	
	public static final String BUILDER_ID = PLUGIN_ID + ".Builder";
	public static final String BUILD_PROBLEM_ID = PLUGIN_ID + ".build_problem";
	public static final String SOURCE_PROBLEM_ID = PLUGIN_ID + ".source_problem";
	
	// Note: the variable should not be named with a prefix of LANGUAGE, 
	// or it will interfere with MelnormeEclipse templating
	public static final String NAME_OF_LANGUAGE = "Rust";
	
	public static final String VAR_NAME_SdkToolPath = "CARGO_TOOL_PATH";
	public static final String VAR_NAME_SdkToolPath_DESCRIPTION = "The path of the Cargo tool";
	
	public static final String LANGUAGE_SERVER_Name = "Racer/RLS";
	
	public LangCore_Actual(ILogHandler logHandler) {
		super(logHandler);
	}
		
	@Override
	protected CoreSettings createCoreSettings() {
		return new CoreSettings() {
			@Override
			public RustSDKLocationValidator getSDKLocationValidator() {
				return new RustSDKLocationValidator();
			}
		};
	}
	
	@Override
	public ToolManager createToolManager() {
		return new RustToolManager(coreSettings);
	}
	
	@Override
	public RustLanguageServerHandler createLanguageServerHandler() {
		return new RustLanguageServerHandler();
	}
	
	public static RustSourceModelManager createSourceModelManager() {
		return new RustSourceModelManager();
	}
	
	
	public static RustBundleModelManager createBundleModelManager() {
		return new RustBundleModelManager();
	}
	
	@Override
	public RustBuildManager createBuildManager() {
		return new RustBuildManager(this.bundleManager.getModel(), getToolManager());
	}
	
}