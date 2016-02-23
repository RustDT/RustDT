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

import static melnorme.utilbox.core.CoreUtil.list;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RustSDKLocationValidator;

import melnorme.lang.ide.ui.ContentAssistPreferences;
import melnorme.lang.ide.ui.preferences.AbstractToolLocationGroup;
import melnorme.lang.ide.ui.preferences.LangSDKConfigBlock;
import melnorme.lang.ide.ui.preferences.common.PreferencesPageContext;
import melnorme.lang.ide.ui.preferences.pages.DownloadToolTextField;
import melnorme.lang.ide.ui.utils.operations.BasicUIOperation;
import melnorme.util.swt.components.IDisableableWidget;
import melnorme.util.swt.components.fields.ButtonTextField;
import melnorme.util.swt.components.fields.CheckBoxField;
import melnorme.util.swt.components.fields.DirectoryTextField;
import melnorme.utilbox.collections.Indexable;

public class RustToolsConfigBlock extends LangSDKConfigBlock {
	
	protected final ButtonTextField sdkSrcLocation = new DirectoryTextField("Rust 'src' Directory:");
	protected final RacerLocationGroup racerGroup = new RacerLocationGroup(); 
	protected final RainicornLocationGroup rainicornGroup = new RainicornLocationGroup();
	protected final RustFmtLocationGroup rustfmtGroup = new RustFmtLocationGroup();
	
	public RustToolsConfigBlock(PreferencesPageContext prefContext) {
		super(prefContext);
		
		bindToDerivedPreference(sdkSrcLocation, RustSDKPreferences.SDK_SRC_PATH3);
		
		addSubComponent(racerGroup);
		addSubComponent(rainicornGroup);
		addSubComponent(rustfmtGroup);
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
	
	public static final Indexable<String> RACER_CargoInstallArgs = list("racer");
	
	public class RacerLocationGroup extends AbstractToolLocationGroup {
		
		public RacerLocationGroup() {
			super("Racer");
			
			bindToDerivedPreference(this.toolLocationField, RustSDKPreferences.RACER_PATH);
			CheckBoxField showErrorsDialogOption = new CheckBoxField(
				"Show error dialog if " + toolName + " failures occur.");
			subwidgets.add(showErrorsDialogOption);
			bindToPreference(showErrorsDialogOption, ContentAssistPreferences.ShowDialogIfContentAssistErrors);
		}
		
		@Override
		protected BasicUIOperation do_getDownloadButtonHandler(DownloadToolTextField downloadToolTextField) {
			return new Start_CargoInstallJob_Operation(toolName, downloadToolTextField,
				RACER_CargoInstallArgs,
				"racer") {
				
				@Override
				protected String getSDKPath() {
					return RustToolsConfigBlock.this.getLocationField().getFieldValue();
				};
			};
		};
		
	}
	
	public static final Indexable<String> RAINICORN_CargoInstallArgs = list(
		"--git", "https://github.com/RustDT/Rainicorn", "--tag", "version_1.x");
	
	public class RainicornLocationGroup extends AbstractToolLocationGroup {
		
		public RainicornLocationGroup() {
			super("Rainicorn parse_describe");
			
			bindToDerivedPreference(this.toolLocationField, RustSDKPreferences.RAINICORN_PATH2);
		}
		
		@Override
		protected Indexable<IDisableableWidget> getSubWidgets() {
			return list(toolLocationField);
		}
		
		@Override
		protected BasicUIOperation do_getDownloadButtonHandler(DownloadToolTextField downloadToolTextField) {
			return new Start_CargoInstallJob_Operation(toolName, downloadToolTextField,
				RAINICORN_CargoInstallArgs,
				"parse_describe") {
				
				@Override
				protected String getSDKPath() {
					return RustToolsConfigBlock.this.getLocationField().getFieldValue();
				};
			};
		};
		
	}
	
	public static final Indexable<String> RUSTFMT_CargoGitSource = list("rustfmt");
	
	public class RustFmtLocationGroup extends AbstractToolLocationGroup {
		
		public final CheckBoxField formatOnSaveField = new CheckBoxField(
			"Format automatically on editor save.");
		
		public RustFmtLocationGroup() {
			super("rustfmt");
			
			bindToDerivedPreference(this.toolLocationField, RustSDKPreferences.RUSTFMT_PATH);
			bindToPreference(this.formatOnSaveField, RustSDKPreferences.FORMAT_ON_SAVE);
		}
		
		@Override
		protected Indexable<IDisableableWidget> getSubWidgets() {
			return list(toolLocationField, formatOnSaveField);
		}
		
		@Override
		protected BasicUIOperation do_getDownloadButtonHandler(DownloadToolTextField downloadToolTextField) {
			
			return new Start_CargoInstallJob_Operation(toolName, downloadToolTextField,
				RUSTFMT_CargoGitSource,
				"rustfmt") {
				
				@Override
				protected String getSDKPath() {
					return RustToolsConfigBlock.this.getLocationField().getFieldValue();
				};
			};
		};
		
	}
	
}