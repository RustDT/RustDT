/*******************************************************************************
 * Copyright (c) 2011, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.preferences;

import melnorme.lang.ide.ui.LangUIPlugin;
import melnorme.lang.ide.ui.preferences.EditorContentAssistConfigurationBlock;
import melnorme.lang.ide.ui.preferences.common.AbstractPreferencesBlockPrefPage;
import melnorme.lang.ide.ui.preferences.common.PreferencesPageContext;

public class RustContentAssistPreferencePage extends AbstractPreferencesBlockPrefPage {
	
	public final static String PAGE_ID = LangUIPlugin.PLUGIN_ID + ".PreferencePages.Editor.ContentAssist";
	
	public RustContentAssistPreferencePage() {
		super();
	}
	
	@Override
	protected EditorContentAssistConfigurationBlock init_createPreferencesBlock(PreferencesPageContext prefContext) {
		return new EditorContentAssistConfigurationBlock(prefContext) {
			@Override
			protected boolean createAutoActivation_DoubleColonOption() {
				return true;
			}
		};
	}
	
	@Override
	protected String getHelpId() {
		return null;
	}
	
}