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
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence_7_7;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.ide.debug.core.AbstractLangDebugLaunchConfigurationDelegate;
import melnorme.lang.ide.debug.core.GdbLaunchDelegateExtension;
import melnorme.lang.ide.debug.core.LangSourceLookupDirector;
import melnorme.lang.ide.debug.core.services.LangDebugServicesExtensions;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.fields.validation.ValidationException;
import melnorme.utilbox.misc.FileUtil;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.MiscUtil;
import melnorme.utilbox.status.StatusException;

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
			
			@Override
			protected Sequence getCompleteInitializationSequence__GDBControl_7_7__ext(DsfSession session,
					Map<String, Object> attributes, RequestMonitorWithProgress rm) {
				return new FinalLaunchSequence_7_7_Rust(session, attributes, rm);
			}
		};
	}
	
	@Override
	protected GdbLaunchDelegateExtension createGdbLaunchDelegate() {
		return new RustGdbLaunchDelegateExt();
	}
	
	public static class GDBBackend_Rust extends GDBBackend_Lang {
		
		protected final Location prettyPrintLoc;
		
		public GDBBackend_Rust(DsfSession session, ILaunchConfiguration lc) {
			super(session, lc);
			
			this.prettyPrintLoc = initGDBPrettyPrintLoc();
		}
		
		protected Location initGDBPrettyPrintLoc() {
			try {
				Location loc = SDK_PATH_Acessor.getSDKLocation(project).resolve_fromValid("rustc/lib/rustlib/etc/");
				if(loc.toFile().exists()) {
					return loc;
				}
				loc = SDK_PATH_Acessor.getSDKLocation(project).resolve_fromValid("bin/rustlib/etc/");
				if(loc.toFile().exists()) {
					return loc;
				}
				return null; // Maybe throw an error?
			} catch (ValidationException ve) {
				LangCore.logWarning(ve.getMessage(), ve.getCause());
				return null;
			}
		}
		
		@Override
		protected String[] getGDBCommandLineArray() {
			ArrayList2<String> gdbCmdLine = new ArrayList2<>(super.getGDBCommandLineArray());
			
			if(prettyPrintLoc != null) {
				
				if(!MiscUtil.OS_IS_WINDOWS) {
					gdbCmdLine.addElements("-d", prettyPrintLoc.toString());
					gdbCmdLine.addElements("-iex", "add-auto-load-safe-path " + prettyPrintLoc.toString());
				} else {
					// GDB pretty printers auto-load isn't working directly in Windows, 
					// so they will be loaded directly later, in initializtion sequence.
				}
			}
			
			return gdbCmdLine.toArray(String.class);
		}
		
		@Override
		protected void customizeEnvironment(HashMap<String, String> envMap) {
			if(prettyPrintLoc == null) {
				return;
			}
			
			String pythonPath = envMap.get("PYTHONPATH");
			pythonPath = pythonPath == null ? "" : pythonPath + File.pathSeparator;
			pythonPath += prettyPrintLoc.toPathString();
			envMap.put("PYTHONPATH", pythonPath);
		}
		
	}
	
	/* -----------------  ----------------- */
	
	public static class FinalLaunchSequence_7_7_Rust extends FinalLaunchSequence_7_7 {
		
		protected IGDBControl fCommandControl;
		protected CommandFactory fCommandFactory;
		
		public FinalLaunchSequence_7_7_Rust(DsfSession session, Map<String, java.lang.Object> attributes,
				RequestMonitorWithProgress rm) {
			super(session, attributes, rm);
		}
		
		@Execute
		@Override
		public void stepInitializeFinalLaunchSequence_7_7(RequestMonitor rm) {
			DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), getSession().getId());
			fCommandControl = tracker.getService(IGDBControl.class);
			tracker.dispose();
			
			if(fCommandControl == null) {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain control service", null));
				rm.done();
				return;
			}
			
			fCommandFactory = fCommandControl.getCommandFactory();
			
			super.stepInitializeFinalLaunchSequence_7_7(rm);
		}
		
		@Execute
		@Override
		public void stepSourceGDBInitFile(RequestMonitor rm) {
			try {
				
				java.nio.file.Path tempFile = WindowsGdbTempFileHelper.getTempFile();
				
				fCommandControl.queueCommand(
					fCommandFactory.createCLISource(fCommandControl.getContext(), tempFile.toString()), 
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleCompleted() {
//							rm.setStatus(getStatus());
							rm.done();
						}
					}
				);
				
				super.stepSourceGDBInitFile(rm);
			} catch(IOException e) {
				rm.setStatus(LangCore.createErrorStatus("Could not create GDB init temp file", e));
				rm.done();
			}
		}
	
	}
	
	public static class WindowsGdbTempFileHelper { 
		
		private static java.nio.file.Path tempFile;
		
		protected static synchronized java.nio.file.Path getTempFile() throws IOException {
			if(tempFile == null) {
				tempFile = Files.createTempFile("rustdt_gdbinit_windows_", "");
				
				Runtime.getRuntime().addShutdownHook(new Thread(() -> tempFile.toFile().delete()));
				
				String contents = "python\n" +
					"print \"--Registering Rust pretty-printers for Windows--\"\n" + 
					"import gdb_rust_pretty_printing\n"+
					"gdb_rust_pretty_printing.register_printers(gdb)\n"+
					"print \"--DONE--\"\n" +
					"end\n";
				FileUtil.writeStringToFile(tempFile.toFile(), contents, melnorme.utilbox.misc.StringUtil.UTF8);
			}
			return tempFile;
		}
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
					
					Location sdkSrcLocation;
					try {
						sdkSrcLocation = RustSDKPreferences.SDK_SRC_PATH3.getDerivedValue(project);
					} catch(StatusException e) {
						LangCore.logWarning("Error setting up source mapping for Rust `src` path.", e);
						return;
					}
					
					// So, this seems to be the mapping that Rust standard lib sources get in debug info.
					// I guess this could change in the future, need to watch out for that.
					
					containers.add(new MapEntrySourceContainer(
						projectLoc.append("../src"),
						ResourceUtils.epath(sdkSrcLocation)
					));
				}
			};
		}
		
	}
	
}