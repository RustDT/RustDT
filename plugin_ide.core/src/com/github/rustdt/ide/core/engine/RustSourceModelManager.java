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
package com.github.rustdt.ide.core.engine;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;

import com.github.rustdt.ide.core.operations.RustSDKPreferences;
import com.github.rustdt.tooling.ops.RustParseDescribeParser;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.engine.SourceModelManager;
import melnorme.lang.ide.core.operations.AbstractToolManager;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.structure.SourceFileStructure;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.core.DevelopmentCodeMarkers;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RustSourceModelManager extends SourceModelManager {
	
	protected final AbstractToolManager toolManager = LangCore.getToolManager();
	
	public RustSourceModelManager() {
	}
	
	@Override
	protected StructureUpdateTask createUpdateTask(StructureInfo structureInfo, String source) {
		return new StructureUpdateTask(structureInfo) {
			@Override
			protected SourceFileStructure createNewData() {
				
				if(DevelopmentCodeMarkers.TESTS_MODE) {
					return null;
				}
				
				ExternalProcessResult describeResult;
				
				Location location = structureInfo.getLocation();
				IProject project = location == null ? null : ResourceUtils.getProject(location);
				try {
					Path path = RustSDKPreferences.RAINICORN_PATH2.getDerivedValue(project);
					
					ProcessBuilder pb = toolManager.createToolProcessBuilder(project, path);
					describeResult = toolManager.runEngineTool(pb, source, cm);
				} catch(OperationCancellation e) {
					return null;
				} catch(CommonException ce) {
					toolManager.logAndNotifyError("Error running parse-describe process:", ce.toStatusError());
					return null;
				}
				
				Location fileLocation = location;
				
				String describeOutput = describeResult.getStdOutBytes().toString(StringUtil.UTF8);
				
				try {
					return new RustParseDescribeParser(fileLocation, source).parse(describeOutput);
				} catch(CommonException ce) {
					toolManager.logAndNotifyError("Error reading parse-describe output:", ce.toStatusError());
					return null;
				}
				
			}
		};
	}
	
}