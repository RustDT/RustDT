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
import static melnorme.utilbox.misc.StringUtil.emptyAsNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.GDBBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ToolchainPreferences;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.ide.debug.core.AbstractLangDebugLaunchConfigurationDelegate;
import melnorme.lang.ide.debug.core.GdbLaunchDelegateExtension;
import melnorme.lang.ide.debug.core.LangSourceLookupDirector;
import melnorme.lang.ide.debug.core.services.LangDebugServicesExtensions;
import melnorme.lang.tooling.data.ValidationException;
import melnorme.utilbox.collections.ArrayList2;
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
	
	@Override
	protected GdbLaunchDelegateExtension createGdbLaunchDelegate() {
		return new RustGdbLaunchDelegateExt();
	}
	
	protected static IProject getProject(ILaunchConfiguration lc) {
		try {
			String prjName = lc.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
			if(emptyAsNull(prjName) != null) {
				return ResourceUtils.getProject(prjName);
			}
		} catch(CoreException e) {
		}
		return null;
	}
	
	public static class GDBBackend_Rust extends GDBBackend {
		
		protected final ILaunchConfiguration fLaunchConfiguration;
		protected final Location prettyPrintLoc;
		protected final IProject project;
		
		public GDBBackend_Rust(DsfSession session, ILaunchConfiguration lc) {
			super(session, lc);
			this.fLaunchConfiguration = lc;
			this.project = getProject(lc);
			
			this.prettyPrintLoc = initGDBPrettyPrintLoc();
		}
		
		protected Location initGDBPrettyPrintLoc() {
			try {
				return SDK_PATH_Acessor.getSDKLocation(project).resolve_fromValid("lib/rustlib/etc/");
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
	
	/* -----------------  Add source lookup for Rust std lib  ----------------- */
	
	protected class RustGdbLaunchDelegateExt extends GdbLaunchDelegateExt {
		
		public RustGdbLaunchDelegateExt() {
		}
		
		@Override
		protected LangSourceLookupDirector createSourceLookupDirector(ILaunchConfiguration lc, DsfSession session) {
			IProject project = getProject(lc); // can be null
			
			if(project == null || project.getLocation() == null) {
				return super.createSourceLookupDirector(lc, session);
			}
			IPath projectLoc = project.getLocation();
			
			return new LangSourceLookupDirector(session) {
				@Override
				protected void customizeDefaultSourceContainers(ArrayList2<ISourceContainer> containers) {
					String sdk_Path = ToolchainPreferences.SDK_PATH.getProjectPreference().getEffectiveValue(project);
					
					// So, this seems to be the mapping that Rust standard lib sources get in debug info.
					// I guess this could change in the future, need to watch out for that.
					
					containers.add(new MapEntrySourceContainer(
						projectLoc.append("../src"),
						new Path(sdk_Path).append("/src"))
					);
				}
			};
		}
		
	}
	
}