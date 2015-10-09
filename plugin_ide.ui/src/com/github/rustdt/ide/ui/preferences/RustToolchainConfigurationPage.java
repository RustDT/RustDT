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
package com.github.rustdt.ide.ui.preferences;

import static com.github.rustdt.ide.core.operations.RustSDKPreferences.RACER_PATH;
import static com.github.rustdt.ide.core.operations.RustSDKPreferences.SDK_SRC_PATH2;

import org.eclipse.core.resources.IProject;

import melnorme.lang.ide.core.operations.ToolchainPreferences;
import melnorme.lang.ide.ui.dialogs.AbstractLangPropertyPage;
import melnorme.lang.ide.ui.preferences.ProjectSDKSettingsBlock;
import melnorme.lang.ide.ui.preferences.common.IPreferencesWidgetComponent;

public class RustToolchainConfigurationPage extends AbstractLangPropertyPage {
	
	@Override
	protected IPreferencesWidgetComponent createProjectConfigComponent(IProject project) {
		return new ProjectSDKSettingsBlock(project, 
			ToolchainPreferences.USE_PROJECT_SETTINGS, 
			ToolchainPreferences.SDK_PATH.getProjectPreference()) {
			
			@Override
			protected RustToolsConfigBlock init_createSDKLocationGroup() {
				RustToolsConfigBlock rustToolsConfigBlock = new RustToolsConfigBlock();
				
				addFieldBinding(rustToolsConfigBlock.sdkSrcLocation, SDK_SRC_PATH2);
				addFieldBinding(rustToolsConfigBlock.racerLocation, RACER_PATH);
				
				return rustToolsConfigBlock;
			}
			
		};
	}
	
}