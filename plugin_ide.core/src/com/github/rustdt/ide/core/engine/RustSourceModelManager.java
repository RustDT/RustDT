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
import melnorme.lang.ide.core.operations.ToolManager;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.structure.SourceFileStructure;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.core.DevelopmentCodeMarkers;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RustSourceModelManager extends SourceModelManager {
	
	protected final ToolManager toolManager = LangCore.getToolManager();
	
	public RustSourceModelManager() {
	}
	
	@Override
	protected StructureUpdateTask createUpdateTask(StructureInfo structureInfo, String source) {
		return new RustStructureUpdateTask(structureInfo, source);
	}
	
	public class RustStructureUpdateTask extends StructureUpdateTask {
		
		protected final String source;
		
		public RustStructureUpdateTask(StructureInfo structureInfo, String source) {
			super(structureInfo);
			this.source = source;
		}
		
		@Override
		protected SourceFileStructure doCreateNewData() throws CommonException, OperationCancellation {
			
			Location fileLocation = structureInfo.getLocation();
			
			String describeOutput = getDescribeOutput(source, fileLocation);
			if(describeOutput == null) {
				return null;
			}
			
			try {
				RustParseDescribeParser parseDescribe = new RustParseDescribeParser(fileLocation, source);
				SourceFileStructure newStructure = parseDescribe.parse(describeOutput);
				if(newStructure.getParserProblems().size() > 0 && newStructure.getChildren().isEmpty()) {
					
					SourceFileStructure previousStructure = structureInfo.getStoredData().getOrNull();
					if(previousStructure != null) {
						// Use elements from previous structure:
						Indexable<StructureElement> previousElements = 
								StructureElement.cloneSubTree(previousStructure.getChildren());
						
						newStructure = new SourceFileStructure(fileLocation, previousElements, 
							newStructure.getParserProblems());
					}
				}
				return newStructure;
			} catch(CommonException ce) {
				throw new CommonException("Error reading parse-describe output:", ce.toStatusException());
				//toolManager.logAndNotifyError("Error reading parse-describe output:", ce.toStatusException());
			}
			
		}
		
		protected String getDescribeOutput(String source, Location fileLocation) 
				throws OperationCancellation, CommonException {
			if(DevelopmentCodeMarkers.TESTS_MODE) {
				return null;
			}
			
			IProject project = fileLocation == null ? null : ResourceUtils.getProjectFromMemberLocation(fileLocation);
				Path path = RustSDKPreferences.RAINICORN_PATH2.getDerivedValue(project);
				
				ProcessBuilder pb = toolManager.createToolProcessBuilder(project, path);
				
				ExternalProcessResult describeResult = toolManager.runEngineTool(pb, source, cm);
				return describeResult.getStdOutBytes().toString(StringUtil.UTF8);
				
		}
	}
	
}