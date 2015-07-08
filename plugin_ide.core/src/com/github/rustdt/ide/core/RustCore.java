package com.github.rustdt.ide.core;

import melnorme.lang.ide.core.LangCore;

import org.osgi.framework.BundleContext;

import com.github.rustdt.ide.core.cargomodel.RustBundleModelManager;

public class RustCore extends LangCore {
	
	protected static final RustBundleModelManager bundleModelManager = new RustBundleModelManager();
	
	public static RustBundleModelManager getBundleModelManager() {
		return bundleModelManager;
	}
	
	@Override
	protected void doCustomStart(BundleContext context) {
	}
	
	@Override
	public void doInitializeAfterUIStart() {
		bundleModelManager.startManager(); // Start this after UI, to allow UI listener to register.
	}
	
	@Override
	protected void doCustomStop(BundleContext context) {
		bundleModelManager.shutdownManager();
	}
	
}