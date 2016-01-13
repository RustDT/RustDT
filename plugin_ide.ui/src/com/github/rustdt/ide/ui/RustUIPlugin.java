package com.github.rustdt.ide.ui;

import melnorme.lang.ide.ui.LangUIPlugin;

import org.osgi.framework.BundleContext;

public class RustUIPlugin extends LangUIPlugin {
	
	@Override
	protected RustOperationsConsoleUIHandler createOperationsConsoleListener() {
		return new RustOperationsConsoleUIHandler();
	}
	
	@Override
	protected void doCustomStop(BundleContext context) {
	}
	
}