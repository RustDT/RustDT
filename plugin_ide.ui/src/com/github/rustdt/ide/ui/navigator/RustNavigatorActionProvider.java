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
package com.github.rustdt.ide.ui.navigator;

import static melnorme.utilbox.core.CoreUtil.list;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IViewPart;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.ide.ui.launch.RustLaunchShortcut;

import melnorme.lang.ide.core.operations.RunToolOperationOnResource;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.ProcessStartKind;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.StartOperationOptions;
import melnorme.lang.ide.ui.LangUIMessages_Actual;
import melnorme.lang.ide.ui.launch.LangLaunchShortcut;
import melnorme.lang.ide.ui.navigator.BuildTargetsActionGroup;
import melnorme.lang.ide.ui.navigator.LangNavigatorActionProvider;
import melnorme.lang.ide.ui.operations.RunToolUIOperation.RunSDKUIToolOperation;
import melnorme.lang.ide.ui.operations.ToolSourceModifyingOperation;
import melnorme.utilbox.core.CommonException;

public class RustNavigatorActionProvider extends LangNavigatorActionProvider {
	
	@Override
	protected BuildTargetsActionGroup createBuildTargetsActionGroup(IViewPart viewPart) {
		return new BuildTargetsActionGroup(viewPart) {
			@Override
			protected LangLaunchShortcut createLaunchShortcut() {
				return new RustLaunchShortcut();
			}
		};
	}
	
	@Override
	protected void initActionGroups(IViewPart viewPart) {
		super.initActionGroups(viewPart);
		actionGroups.add(new RustBundleOperationsActionGroup(viewPart));
	}
	
	public static class RustBundleOperationsActionGroup extends BundleOperationsActionGroup {
		
		public RustBundleOperationsActionGroup(IViewPart viewPart) {
			super(viewPart);
		}
		
		@Override
		protected void initActions(MenuManager bundleOpsMenu, IProject project) {
			addRunOperationAction(bundleOpsMenu, 
				new RunSDKUIToolOperation(LangUIMessages_Actual.CargoUpdate_OpName, project, list("update")));
			addRunOperationAction(bundleOpsMenu, 
				new CargoFormatOperation(project));
		}
		
		@Override
		protected String getMenuName() {
			return LangUIMessages_Actual.BundleOperationsMenu;
		}
		
	}
	
	public static class CargoFormatOperation extends ToolSourceModifyingOperation {
		public CargoFormatOperation(IProject project) {
			super(
				LangUIMessages_Actual.CargoFormat_OpName, 
				new RunToolOperationOnResource(
					project, 
					list("--", "--write-mode=overwrite"), 
					new StartOperationOptions(ProcessStartKind.BUILD, true, true)
				) {
					@Override
					protected ProcessBuilder createProcessBuilder() throws CommonException {
						Path rustfmtPath = RustSDKPreferences.RUSTFMT_PATH.getDerivedValue();
						// Look for sibling command cargo-fmt
						Path cargoFmtPath = rustfmtPath.getParent().resolve("cargo-fmt");
						
						return getToolManager().createToolProcessBuilder(project, cargoFmtPath, getCommands());
					}
				}
				
			);
		}
		
	}
	
}