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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;

import melnorme.lang.ide.ui.preferences.LangSDKConfigBlock;
import melnorme.util.swt.SWTFactoryUtil;
import melnorme.util.swt.components.AbstractComponentExt;
import melnorme.util.swt.components.fields.ButtonTextField;
import melnorme.util.swt.components.fields.DirectoryTextField;
import melnorme.util.swt.components.fields.FileTextField;

public class RustToolsConfigBlock extends LangSDKConfigBlock {
	
	protected final ButtonTextField sdkSrcLocation = new DirectoryTextField("Rust 'src' Directory:");
	protected final RacerLocationGroup racerGroup = new RacerLocationGroup(); 
	protected final ButtonTextField racerLocation = racerGroup.racerLocation;
	
	public RustToolsConfigBlock() {
	}
	
	@Override
	protected void createContents(Composite topControl) {
		super.createContents(topControl);
		
		racerGroup.createComponent(topControl, gdFillDefaults().grab(true, false).create());
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		sdkSrcLocation.setEnabled(enabled);
		racerGroup.setEnabled(enabled);
	}
	
	@Override
	protected LanguageSDKLocationGroup createSDKLocationGroup() {
		return new LanguageSDKLocationGroup() {
			
			@Override
			protected void createContents(Composite topControl) {
				super.createContents(topControl);
				sdkSrcLocation.createComponentInlined(topControl);
			}
			
		};
	}
	
	public class RacerLocationGroup extends AbstractComponentExt {
		
		public final ButtonTextField racerLocation = new FileTextField("Executable:");
		
		@Override
		protected Composite doCreateTopLevelControl(Composite parent) {
			return SWTFactoryUtil.createGroup(parent, "Racer installation: ");
		}
		
		@Override
		protected GridLayoutFactory createTopLevelLayout() {
			return GridLayoutFactory.swtDefaults().numColumns(getPreferredLayoutColumns());
		}
		
		@Override
		public int getPreferredLayoutColumns() {
			return 3;
		}
		
		@Override
		protected void createContents(Composite topControl) {
			racerLocation.createComponentInlined(topControl);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			racerLocation.setEnabled(enabled);
		}
		
		@Override
		public void updateComponentFromInput() {
		}
	}
	
}