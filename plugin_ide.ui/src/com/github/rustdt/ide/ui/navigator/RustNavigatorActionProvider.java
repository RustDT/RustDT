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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IViewPart;

import com.github.rustdt.ide.ui.launch.RustLaunchShortcut;

import melnorme.lang.ide.ui.LangUIMessages_Actual;
import melnorme.lang.ide.ui.launch.LangLaunchShortcut;
import melnorme.lang.ide.ui.navigator.BuildTargetsActionGroup;
import melnorme.lang.ide.ui.navigator.LangNavigatorActionProvider;
import melnorme.lang.ide.ui.operations.RunToolOperation.RunSDKToolOperation;

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
		actionGroups.add(new DubPathActionGroup(viewPart));
	}
	
	public static class DubPathActionGroup extends BundleOperationsActionGroup {
		
		public DubPathActionGroup(IViewPart viewPart) {
			super(viewPart);
		}
		
		@Override
		protected void initActions(MenuManager bundleOpsMenu, IProject project) {
			addRunOperationAction(bundleOpsMenu, new AddDubProjectToLocalPath(project));
		}
		
		@Override
		protected String getMenuName() {
			return LangUIMessages_Actual.BundleOperationsMenu;
		}
		
		public class AddDubProjectToLocalPath extends RunSDKToolOperation {
			public AddDubProjectToLocalPath(IProject project) {
				super(LangUIMessages_Actual.CargoUpdate_OpName, project,
					list("update"));
			}
		}
		
	}
	
}