package com.github.rustdt.ide.debug.ui;

import melnorme.lang.ide.debug.ui.AbstractLangDebugTabGroup;

import org.eclipse.debug.ui.ILaunchConfigurationTab;

import com.github.rustdt.ide.ui.launch.RustMainLaunchConfigurationTab;


public class RustDebugTabGroup extends AbstractLangDebugTabGroup {
	
	@Override
	protected ILaunchConfigurationTab createMainLaunchConfigTab() {
		return new RustMainLaunchConfigurationTab();
	}
	
}