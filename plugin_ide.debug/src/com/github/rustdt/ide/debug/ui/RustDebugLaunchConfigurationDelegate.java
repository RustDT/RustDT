/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.debug.ui;


import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.debug.core.AbstractLangDebugLaunchConfigurationDelegate;
import melnorme.lang.ide.debug.core.services.DebugServicesExtensions;
import melnorme.lang.tooling.data.AbstractValidator.ValidationException;
import melnorme.utilbox.misc.ArrayUtil;
import melnorme.utilbox.misc.Location;

import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.GDBBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;

public class RustDebugLaunchConfigurationDelegate extends AbstractLangDebugLaunchConfigurationDelegate {
	
	@Override
	protected GdbLaunch doCreateGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator) {
		return new RustGdbLaunch(configuration, mode, locator);
	}
	
	@Override
	protected DebugServicesExtensions doCreateServicesExtensions() {
		return new DebugServicesExtensions() {
			@Override
			public IMIBackend createBackendGDBService(DsfSession session, ILaunchConfiguration lc) {
				return new GDBBackend_Rust(session, lc);
			}
		};
	}
	
	public static class GDBBackend_Rust extends GDBBackend {
		public GDBBackend_Rust(DsfSession session, ILaunchConfiguration lc) {
			super(session, lc);
		}
		
		@Override
		protected String[] getGDBCommandLineArray() {
			String[] gdbCmdLine = super.getGDBCommandLineArray();
			Location loc = null;
			try {
				loc = RustSDKPreferences.SDK_PATH_Acessor.getSDKLocation().resolve_fromValid("lib/rustlib/etc/");
			} catch (ValidationException ve) {
				LangCore.logWarning(ve.getMessage(), ve.getCause());
				return gdbCmdLine;
			}
			
			gdbCmdLine =ArrayUtil.concat(gdbCmdLine, "-d", loc.toPathString());
			gdbCmdLine =ArrayUtil.concat(gdbCmdLine, "-iex", "add-auto-load-safe-path " + loc.toPathString());
//			gdbCmdLine =ArrayUtil.concat(gdbCmdLine, "-iex", "source " + 
//					loc.resolve_fromValid("gdb_rust_pretty_printing.py").toPathString());
			return gdbCmdLine;
		}
	}
	
}