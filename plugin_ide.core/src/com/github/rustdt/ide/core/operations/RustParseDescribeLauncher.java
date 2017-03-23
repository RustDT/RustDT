package com.github.rustdt.ide.core.operations;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;

import melnorme.lang.ide.core.operations.ToolManager;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.utilbox.concurrency.ICancelMonitor;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.core.DevelopmentCodeMarkers;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RustParseDescribeLauncher {
	protected final ToolManager toolManager;
	protected final ICancelMonitor cancelMonitor;
	
	public RustParseDescribeLauncher(ToolManager toolManager, ICancelMonitor cancelMonitor) {
		this.toolManager = toolManager;
		this.cancelMonitor = cancelMonitor;
	}
	
	public String getDescribeOutput(String source, Location fileLocation)
			throws OperationCancellation, CommonException {
		if(DevelopmentCodeMarkers.TESTS_MODE) {
			return "RUST_PARSE_DESCRIBE 1.0 { MESSAGES {} }";
		}
		
		IProject project = fileLocation == null ? null : ResourceUtils.getProjectFromMemberLocation(fileLocation);
		Path path = RustSDKPreferences.RAINICORN_PATH2.getDerivedValue(project);
		
		ProcessBuilder pb = toolManager.createToolProcessBuilder(project, path);
		
		ExternalProcessResult describeResult = toolManager.runEngineTool(pb, source, cancelMonitor);
		return describeResult.getStdOutBytes().toString(StringUtil.UTF8);
	}
}
