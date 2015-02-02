package com.github.rustdt.ide.ui.launch;

import melnorme.lang.ide.ui.launch.MainLaunchConfigurationTab;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;


public class RustMainLaunchConfigurationTab extends MainLaunchConfigurationTab {
	
	public RustMainLaunchConfigurationTab() {
		super();
	}
	
	@Override
	protected void programPathField_setDefaults(IResource contextualResource, ILaunchConfigurationWorkingCopy config) {
		// XXX
	}
	
}