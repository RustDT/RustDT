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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RacerOperation.RustRacerLocationValidator;
import com.github.rustdt.tooling.ops.RustSDKLocationValidator;
import com.github.rustdt.tooling.ops.RustSDKSrcLocationValidator;

import melnorme.lang.ide.ui.ContentAssistPreferences;
import melnorme.lang.ide.ui.preferences.LangSDKConfigBlock;
import melnorme.lang.ide.ui.preferences.common.PreferencesPageContext;
import melnorme.lang.ide.ui.preferences.pages.DownloadToolTextField;
import melnorme.lang.ide.ui.utils.operations.BasicUIOperation;
import melnorme.util.swt.SWTFactoryUtil;
import melnorme.util.swt.components.AbstractCompositeWidget;
import melnorme.util.swt.components.FieldComponent;
import melnorme.util.swt.components.IDisableableWidget;
import melnorme.util.swt.components.fields.ButtonTextField;
import melnorme.util.swt.components.fields.CheckBoxField;
import melnorme.util.swt.components.fields.DirectoryTextField;
import melnorme.utilbox.collections.Indexable;

public class RustToolsConfigBlock extends LangSDKConfigBlock {
	
	protected final ButtonTextField sdkSrcLocation = new DirectoryTextField("Rust 'src' Directory:");
	protected final RacerLocationGroup racerGroup = new RacerLocationGroup(); 
	protected final ButtonTextField racerLocation = racerGroup.racerLocation;
	
	public RustToolsConfigBlock(PreferencesPageContext prefContext) {
		super(prefContext);
		
		bindToPreference(sdkSrcLocation, RustSDKPreferences.SDK_SRC_PATH2);
		bindToPreference(racerLocation, RustSDKPreferences.RACER_PATH);
		bindToPreference(racerGroup.showErrorsDialog, ContentAssistPreferences.ShowDialogIfContentAssistErrors);
		
		validation.addFieldValidation(true, sdkSrcLocation, new RustSDKSrcLocationValidator());
		validation.addFieldValidation(true, racerLocation, new RustRacerLocationValidator());
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
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		racerGroup.setEnabled(enabled);
	}
	
	public class RacerLocationGroup extends AbstractCompositeWidget {
		
		@Override
		protected Composite doCreateTopLevelControl(Composite parent) {
			return SWTFactoryUtil.createGroup(parent, "Racer: ");
		}
		
		@Override
		protected GridLayoutFactory createTopLevelLayout() {
			return GridLayoutFactory.swtDefaults().numColumns(getPreferredLayoutColumns());
		}
		
		@Override
		public int getPreferredLayoutColumns() {
			return 4;
		}
		
		@Override
		protected Indexable<IDisableableWidget> getSubWidgets() {
			return list(racerLocation, showErrorsDialog);
		}
		
		public final ButtonTextField racerLocation = new DownloadToolTextField("Executable:", "Download...") {
			@Override
			public BasicUIOperation getDownloadButtonHandler() {
				return new Start_CargoInstallJob_Operation("Racer", this,
					RustSDKPreferences.RACER_CargoGitSource, RustSDKPreferences.RACER_CargoGitTag,
					"racer") {
					
					@Override
					protected String getSDKPath() {
						return RustToolsConfigBlock.this.getLocationField().getFieldValue();
					};
				};
			};
		};
		
		public final FieldComponent<Boolean> showErrorsDialog = new CheckBoxField(
			"Show error dialog if " + "racer" + " failures occur during Content Assist");
		
	}
	
}