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

import com.github.rustdt.ide.core.engine.RustStructureUpdateTask.RustStructureSourceTouchedTask;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.engine.SourceModelManager;
import melnorme.lang.ide.core.engine.StructureUpdateTask;
import melnorme.lang.ide.core.operations.ToolManager;

public class RustSourceModelManager extends SourceModelManager {
	
	protected final ToolManager toolManager = LangCore.getToolManager();
	
	public RustSourceModelManager() {
	}
	
	@Override
	protected StructureUpdateTask createUpdateTask(StructureInfo structureInfo, String source) {
		return new RustStructureSourceTouchedTask(structureInfo, structureInfo.getLocation(), () -> source);
	}
}