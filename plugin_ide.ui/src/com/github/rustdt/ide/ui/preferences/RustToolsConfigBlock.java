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
import melnorme.util.swt.SWTFactoryUtil;
import melnorme.util.swt.components.AbstractComponentExt;
import melnorme.util.swt.components.AbstractCompositeComponent;
import melnorme.util.swt.components.FieldComponent;
import melnorme.util.swt.components.fields.ButtonTextField;
import melnorme.util.swt.components.fields.CheckBoxField;
import melnorme.util.swt.components.fields.DirectoryTextField;
import melnorme.util.swt.components.fields.FileTextField;
import melnorme.utilbox.collections.Indexable;

public class RustToolsConfigBlock extends LangSDKConfigBlock {
	
	protected final ButtonTextField sdkSrcLocation = new DirectoryTextField("Rust 'src' Directory:");
	protected final RacerLocationGroup racerGroup = new RacerLocationGroup(); 
	protected final ButtonTextField racerLocation = racerGroup.racerLocation;
	
	public RustToolsConfigBlock(PreferencesPageContext prefContext) {
		super(prefContext);
		
		bindToPreference(sdkSrcLocation, RustSDKPreferences.SDK_SRC_PATH2.getGlobalPreference());
		bindToPreference(racerLocation, RustSDKPreferences.RACER_PATH.getGlobalPreference());
		bindToPreference(racerGroup.showErrorsDialog, 
			ContentAssistPreferences.ShowDialogIfContentAssistErrors.getGlobalPreference());
		
		validation.addFieldValidation(true, sdkSrcLocation, new RustSDKSrcLocationValidator());
		validation.addFieldValidation(true, racerLocation, new RustRacerLocationValidator());
	}
	
	@Override
	protected LanguageSDKLocationGroup init_createSDKLocationGroup() {
		return new LanguageSDKLocationGroup() {
			@Override
			protected Indexable<AbstractComponentExt> getSubComponents() {
				return super.getSubComponents().toArrayList().addElements(sdkSrcLocation);
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
	
	public class RacerLocationGroup extends AbstractCompositeComponent {
		
		public final ButtonTextField racerLocation = new FileTextField("Executable:");
		public final FieldComponent<Boolean> showErrorsDialog = new CheckBoxField(
			"Show error dialog if " + "racer" + " failures occur during Content Assist");
		
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
		protected Indexable<AbstractComponentExt> getSubComponents() {
			return list(racerLocation, showErrorsDialog);
		}
		
		@Override
		public void updateComponentFromInput() {
		}
	}
	
}