package com.github.rustdt.ide.ui.launch;

import org.eclipse.debug.ui.ILaunchConfigurationTab;

import melnorme.lang.ide.ui.launch.AbstractLangTabGroup;


public class RustTabGroup extends AbstractLangTabGroup {
	
	@Override
	protected ILaunchConfigurationTab createMainLaunchConfigTab() {
		return new RustMainLaunchConfigurationTab();
	}
	
}