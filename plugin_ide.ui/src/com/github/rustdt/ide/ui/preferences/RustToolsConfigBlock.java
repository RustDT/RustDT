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

import static com.github.rustdt.ide.ui.preferences.Start_CargoInstallJob_Operation.dlArgs;
import static melnorme.utilbox.core.CoreUtil.list;

import org.eclipse.swt.widgets.Composite;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RustSDKLocationValidator;
import com.github.rustdt.tooling.ops.RustSDKSrcLocationValidator;

import melnorme.lang.ide.ui.ContentAssistPreferences;
import melnorme.lang.ide.ui.preferences.AbstractToolLocationGroup;
import melnorme.lang.ide.ui.preferences.LangSDKConfigBlock;
import melnorme.lang.ide.ui.preferences.common.PreferencesPageContext;
import melnorme.lang.ide.ui.preferences.pages.DownloadToolTextField;
import melnorme.lang.ide.ui.utils.operations.BasicUIOperation;
import melnorme.util.swt.components.IDisableableWidget;
import melnorme.util.swt.components.fields.ButtonTextField;
import melnorme.util.swt.components.fields.DirectoryTextField;
import melnorme.utilbox.collections.Indexable;

public class RustToolsConfigBlock extends LangSDKConfigBlock {
	
	protected final ButtonTextField sdkSrcLocation = new DirectoryTextField("Rust 'src' Directory:");
	protected final RacerLocationGroup racerGroup = new RacerLocationGroup(); 
	protected final RainicornLocationGroup rainicornGroup = new RainicornLocationGroup();
	
	public RustToolsConfigBlock(PreferencesPageContext prefContext) {
		super(prefContext);
		
		validation.addFieldValidation(true, sdkSrcLocation, new RustSDKSrcLocationValidator());
		validation.addValidatableField(true, racerGroup.getStatusField());
		validation.addValidatableField(true, rainicornGroup.getStatusField());
		
		bindToPreference(sdkSrcLocation, RustSDKPreferences.SDK_SRC_PATH2);
	}
	
	@Override
	protected LanguageSDKLocationGroup init_createSDKLocationGroup() {
		return new LanguageSDKLocationGroup() {
			@Override
			protected Indexable<IDisableableWidget> getSubWidgets() {
				return super.getSubWidgets().toArrayList().addElements(sdkSrcLocation);
			}
		};
	}
	
	@Override
	protected RustSDKLocationValidator getSDKValidator() {
		return new RustSDKLocationValidator();
	}
	
	@Override
	protected void createContents(Composite topControl) {
		super.createContents(topControl);
		
		racerGroup.createComponent(topControl, gdFillDefaults().grab(true, false).create());
		rainicornGroup.createComponent(topControl, gdFillDefaults().grab(true, false).create());
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		racerGroup.setEnabled(enabled);
		rainicornGroup.setEnabled(enabled);
	}
	
	public class RacerLocationGroup extends AbstractToolLocationGroup {
		
		public RacerLocationGroup() {
			super("Racer");
			
			bindToDerivedPreference(this.toolLocation, RustSDKPreferences.RACER_PATH);
			bindToPreference(this.showErrorsDialogOption, ContentAssistPreferences.ShowDialogIfContentAssistErrors);
		}
		
		@Override
		protected BasicUIOperation do_getDownloadButtonHandler(DownloadToolTextField downloadToolTextField) {
			return new Start_CargoInstallJob_Operation(toolName, downloadToolTextField,
				list("racer"),
				"racer") {
				
				@Override
				protected String getSDKPath() {
					return RustToolsConfigBlock.this.getLocationField().getFieldValue();
				};
			};
		};
		
	}
	
	public class RainicornLocationGroup extends AbstractToolLocationGroup {
		
		public RainicornLocationGroup() {
			super("Rainicorn parse_describe");
			
			bindToDerivedPreference(this.toolLocation, RustSDKPreferences.RAINICORN_PATH2);
		}
		
		@Override
		protected Indexable<IDisableableWidget> getSubWidgets() {
			return list(toolLocation);
		}
		
		@Override
		protected BasicUIOperation do_getDownloadButtonHandler(DownloadToolTextField downloadToolTextField) {
			return new Start_CargoInstallJob_Operation(toolName, downloadToolTextField,
				dlArgs(RustSDKPreferences.RAINICORN_CargoGitSource, RustSDKPreferences.RAINICORN_CargoGitTag),
				"parse_describe") {
				
				@Override
				protected String getSDKPath() {
					return RustToolsConfigBlock.this.getLocationField().getFieldValue();
				};
			};
		};
		
	}
	
}