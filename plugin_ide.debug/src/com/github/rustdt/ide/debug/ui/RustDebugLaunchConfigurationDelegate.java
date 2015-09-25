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
package com.github.rustdt.ide.debug.ui;


import static com.github.rustdt.ide.core.operations.RustSDKPreferences.SDK_PATH_Acessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.GDBBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.debug.core.AbstractLangDebugLaunchConfigurationDelegate;
import melnorme.lang.ide.debug.core.services.LangDebugServicesExtensions;
import melnorme.lang.tooling.data.ValidationException;
import melnorme.utilbox.misc.ArrayUtil;
import melnorme.utilbox.misc.Location;

public class RustDebugLaunchConfigurationDelegate extends AbstractLangDebugLaunchConfigurationDelegate {
	
	@Override
	protected GdbLaunch doCreateGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator) {
		return new RustGdbLaunch(configuration, mode, locator);
	}
	
	@Override
	protected LangDebugServicesExtensions doCreateServicesExtensions(IDsfDebugServicesFactory parentServiceFactory) {
		return new LangDebugServicesExtensions(parentServiceFactory) {
			@Override
			public IMIBackend createBackendGDBService(DsfSession session, ILaunchConfiguration lc) {
				return new GDBBackend_Rust(session, lc);
			}
		};
	}
	
	public static class GDBBackend_Rust extends GDBBackend {
		
		protected final ILaunchConfiguration fLaunchConfiguration;
		protected final Location prettyPrintLoc;
		
		public GDBBackend_Rust(DsfSession session, ILaunchConfiguration lc) {
			super(session, lc);
			this.fLaunchConfiguration = lc;
			
			prettyPrintLoc = initGDBPrettyPrintLoc();
		}
		
		protected Location initGDBPrettyPrintLoc() {
			try {
				return SDK_PATH_Acessor.getSDKLocation().resolve_fromValid("lib/rustlib/etc/");
			} catch (ValidationException ve) {
				LangCore.logWarning(ve.getMessage(), ve.getCause());
				return null;
			}
		}
		
		@Override
		protected String[] getGDBCommandLineArray() {
			String[] gdbCmdLine = super.getGDBCommandLineArray();
			
			if(prettyPrintLoc != null) {
				gdbCmdLine = ArrayUtil.concat(gdbCmdLine, "-d", prettyPrintLoc.toPathString());
				gdbCmdLine = ArrayUtil.concat(gdbCmdLine, "-iex", 
					"add-auto-load-safe-path " + prettyPrintLoc.toPathString());
			}
			
			return gdbCmdLine;
		}
		
		@Override
		protected Process launchGDBProcess(String[] commandLine) throws CoreException {
			String[] launchEnvironment = LaunchUtils.getLaunchEnvironment(fLaunchConfiguration);
			if(prettyPrintLoc != null) {
				// launchEnvironment should be null for non-CDT projects anyways
				if(launchEnvironment != null) {
					LangCore.logWarning("Ignoring previous CDT GDB launch environment");
				}
				launchEnvironment = getModifiedPythonEnvironment();
			}
			try {
				return ProcessFactory.getFactory().exec(commandLine, launchEnvironment);
			} catch (IOException e) {
			    String message = "Error while launching command: " + StringUtil.join(commandLine, " "); //$NON-NLS-1$ //$NON-NLS-2$
			    throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, message, e));
			}
		}
		
		protected String[] getModifiedPythonEnvironment() {
			HashMap<String, String> envMap = new HashMap<>(System.getenv());
			String pythonPath = envMap.get("PYTHONPATH");
			pythonPath = pythonPath == null ? "" : pythonPath + File.pathSeparator;
			pythonPath += prettyPrintLoc.toPathString();
			envMap.put("PYTHONPATH", pythonPath);
			
			return convertoToEnvpFormat(envMap);
		}
		
	}
	
	protected static String[] convertoToEnvpFormat(HashMap<String, String> envMap) {
		List<String> envp = new ArrayList<>(envMap.size());
		for(Entry<String, String> entry : envMap.entrySet()) {
			envp.add(entry.getKey() + "=" + entry.getValue());
		}
		return ArrayUtil.createFrom(envp, String.class);
	}
	
}