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
package com.github.rustdt.ide.ui.editor.structure;

import melnorme.lang.ide.ui.editor.structure.StructureModelManager;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.structure.IStructureElement;
import melnorme.lang.tooling.structure.SourceFileStructure;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.lang.tooling.structure.StructureElementData;
import melnorme.lang.tooling.structure.StructureElementKind;
import melnorme.lang.utils.M_WorkerThread;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.misc.Location;

public class RustStructureModelManager extends StructureModelManager {
	
	public RustStructureModelManager() {
	}
	
	@Override
	public void rebuild(Location location, String source, M_WorkerThread reconcilerWorkerThread) {
		// TODO: LANG
		
		StructureElement fakeElement = new StructureElement("NOT_IMPLEMENT", new SourceRange(0, source.length()), 
			new SourceRange(0, source.length()), StructureElementKind.MODULEDEC, new StructureElementData(), null, null);
		addNewStructure(location, new SourceFileStructure(location, new ArrayList2<IStructureElement>(fakeElement)));
	}
	
}