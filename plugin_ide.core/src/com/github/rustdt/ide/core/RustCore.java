package com.github.rustdt.ide.core;

import melnorme.lang.ide.core.LangCore;

import org.osgi.framework.BundleContext;

import com.github.rustdt.ide.core.cargomodel.CargoModelManager;

public class RustCore extends LangCore {
	
	public static final String PLUGIN_ID = "com.github.rustdt.ide.core";
	
	protected static final CargoModelManager bundleModelManager = new CargoModelManager();
	
	public static CargoModelManager getBundleModelManager() {
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