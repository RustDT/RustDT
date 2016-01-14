/*******************************************************************************
 * Copyright (c) 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.ide.core.operations;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import melnorme.lang.ide.core.ILangOperationsListener;
import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.IOperationConsoleHandler;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.ProcessStartKind;
import melnorme.lang.ide.core.operations.ILangOperationsListener_Default.StartOperationOptions;
import melnorme.lang.ide.core.utils.EclipseUtils;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.ide.core.utils.operation.EclipseCancelMonitor;
import melnorme.lang.ide.core.utils.process.AbstractRunProcessTask;
import melnorme.lang.ide.core.utils.process.AbstractRunProcessTask.ProcessStartHelper;
import melnorme.lang.tooling.data.StatusException;
import melnorme.lang.tooling.data.StatusLevel;
import melnorme.lang.tooling.ops.IOperationService;
import melnorme.lang.tooling.ops.util.PathValidator;
import melnorme.lang.utils.ProcessUtils;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.ICancelMonitor;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.fields.EventSource;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Abstract class for running external tools and notifying interested listeners (normally the UI only).
 */
public abstract class AbstractToolManager extends EventSource<ILangOperationsListener> {
	
	public AbstractToolManager() {
	}
	
	public void shutdownNow() {
	}
	
	/* -----------------  ----------------- */
	
	public Path getSDKToolPath(IProject project) throws CommonException {
		Path validatedPath = getSDKToolPathValidator().getValidatedPath(getSDKPathPreference(project));
		assertNotNull(validatedPath);
		return validatedPath;
	}
	
	public String getSDKPathPreference(IProject project) {
		return ToolchainPreferences.SDK_PATH2.getEffectiveValue(project);
	}
	
	public abstract PathValidator getSDKToolPathValidator();
	
	/* -----------------  ----------------- */
	
	public ProcessBuilder createSDKProcessBuilder(IProject project, String... sdkOptions)
			throws CommonException {
		Path sdkToolPath = getSDKToolPath(project);
		return createToolProcessBuilder(project, sdkToolPath, sdkOptions);
	}
	
	public ProcessBuilder createToolProcessBuilder(IProject project, Path sdkToolPath, String... sdkOptions)
			throws CommonException {
		Location projectLocation = project == null ? null : ResourceUtils.getProjectLocation2(project);
		return createToolProcessBuilder(sdkToolPath, projectLocation, sdkOptions);
	}
	
	public ProcessBuilder createToolProcessBuilder(Path buildToolCmdPath, Location workingDir, String... arguments) {
		return ProcessUtils.createProcessBuilder(buildToolCmdPath, workingDir, true, arguments);
	}
	
	public static ProcessBuilder createProcessBuilder(IProject project, String... commands) {
		Path workingDir = project != null ?
			project.getLocation().toFile().toPath() :
			EclipseUtils.getWorkspaceRoot().getLocation().toFile().toPath();
		return new ProcessBuilder(commands).directory(workingDir.toFile());
	}
	
	/* -----------------  ----------------- */
	
	public void notifyMessage(StatusLevel statusLevel, String title, String message) {
		for(ILangOperationsListener listener : getListeners()) {
			listener.notifyMessage(statusLevel, title, message);
		}
	}
	
	/* ----------------- ----------------- */
	
	protected EclipseCancelMonitor cm(IProgressMonitor pm) {
		return new EclipseCancelMonitor(pm);
	}
	
	public IOperationConsoleHandler startNewBuildOperation() {
		return startNewBuildOperation(false);
	}
	
	public IOperationConsoleHandler startNewBuildOperation(boolean explicitConsoleNotify) {
		return startNewOperation(ProcessStartKind.BUILD, true, explicitConsoleNotify);
	}
	
	public IOperationConsoleHandler startNewOperation(ProcessStartKind kind, boolean clearConsole, 
			boolean activateConsole) {
		return startNewOperation(new StartOperationOptions(kind, clearConsole, activateConsole));
	}
	
	public IOperationConsoleHandler startNewOperation(StartOperationOptions options) {
		
		AggregatedOperationConsoleHandler aggregatedHandlers = new AggregatedOperationConsoleHandler();
		
		for(ILangOperationsListener processListener : getListeners()) {
			IOperationConsoleHandler handler = processListener.beginOperation(options);
			aggregatedHandlers.handlers.add(handler);
		}
		return aggregatedHandlers;
	}
	
	public static class AggregatedOperationConsoleHandler implements IOperationConsoleHandler {
		
		public final ArrayList2<IOperationConsoleHandler> handlers = new ArrayList2<>();
		
		public AggregatedOperationConsoleHandler() {
		}
		
		@Override
		public void handleProcessStart(String prefixText, ProcessBuilder pb, ProcessStartHelper processStartHelper) {
			for (IOperationConsoleHandler handler : handlers) {
				handler.handleProcessStart(prefixText, pb, processStartHelper);
			}
		}
		
		@Override
		public void writeInfoMessage(String operationMessage) {
			for (IOperationConsoleHandler handler : handlers) {
				handler.writeInfoMessage(operationMessage);
			}
		}
		
		@Override
		public void activate() {
			for (IOperationConsoleHandler handler : handlers) {
				handler.activate();
			}
		}
		
	}
	
	/* -----------------  ----------------- */
	
	public final RunToolTask newRunBuildToolOperation(ProcessBuilder pb, IProgressMonitor pm) {
		IOperationConsoleHandler opHandler = startNewBuildOperation();
		return newRunProcessTask(opHandler, pb, pm);
	}
	
	public final RunToolTask newRunProcessTask(IOperationConsoleHandler handler, ProcessBuilder pb, 
			IProgressMonitor pm) {
		return newRunProcessTask(handler, pb, cm(pm));
	}
	public RunToolTask newRunProcessTask(IOperationConsoleHandler handler, ProcessBuilder pb, ICancelMonitor cm) {
		String prefixText = ">> Running: ";
		return new RunToolTask(handler, prefixText, pb, cm);
	}
	
	public class RunToolTask extends AbstractRunProcessTask {
		
		protected final IOperationConsoleHandler handler;
		protected final String prefixText;
		
		public RunToolTask(IOperationConsoleHandler handler, ProcessBuilder pb, ICancelMonitor cm) {
			this(handler, null, pb, cm);
		}
		
		public RunToolTask(IOperationConsoleHandler handler, String prefixText, 
				ProcessBuilder pb, ICancelMonitor cm) {
			super(pb, cm);
			this.prefixText = prefixText;
			this.handler = assertNotNull(handler);
		}
		
		@Override
		protected void handleProcessStartResult(ProcessStartHelper psh) {
			handler.handleProcessStart(prefixText, pb, psh);
		}
		
	}
	
	/* ----------------- ----------------- */
	
	public ExternalProcessResult runEngineTool(ProcessBuilder pb, String clientInput, IProgressMonitor pm) 
			throws CoreException, OperationCancellation {
		return runEngineTool(pb, clientInput, cm(pm));
	}
	
	public ExternalProcessResult runEngineTool(ProcessBuilder pb, String clientInput, ICancelMonitor cm) 
			throws CoreException, OperationCancellation {
		try {
			IOperationConsoleHandler handler = startNewOperation(ProcessStartKind.ENGINE_TOOLS, false, false);
			return new RunToolTask(handler, pb, cm).runProcess(clientInput);
		} catch(CommonException ce) {
			throw LangCore.createCoreException(ce);
		}
	}
	
	/* -----------------  ----------------- */
	
	/** 
	 * Helper to start engine client processes in the tool manager. 
	 */
	public class ToolManagerEngineToolRunner implements IOperationService {
		
		protected final boolean throwOnNonZeroStatus;
		protected final EclipseCancelMonitor cm;
		
		public ToolManagerEngineToolRunner(IProgressMonitor monitor, boolean throwOnNonZeroStatus) {
			this.throwOnNonZeroStatus = throwOnNonZeroStatus;
			this.cm = new EclipseCancelMonitor(monitor);
		}
		
		@Override
		public ExternalProcessResult runProcess(ProcessBuilder pb, String input) throws CommonException,
				OperationCancellation {
			IOperationConsoleHandler handler = startNewOperation(ProcessStartKind.ENGINE_TOOLS, false, false);
			return new RunToolTask(handler, pb, cm).runProcess(input, throwOnNonZeroStatus);
		}
		
		@Override
		public void logStatus(StatusException statusException) {
			LangCore.logStatusException(statusException);
		}
		
	}
	
}