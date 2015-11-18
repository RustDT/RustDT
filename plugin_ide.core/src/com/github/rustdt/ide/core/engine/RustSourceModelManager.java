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

import melnorme.lang.ide.core.engine.SourceModelManager;
import melnorme.lang.tooling.ElementAttributes;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.structure.SourceFileStructure;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.lang.tooling.structure.StructureElementKind;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.misc.Location;

public class RustSourceModelManager extends SourceModelManager {
	
	public RustSourceModelManager() {
	}
	
	@Override
	protected StructureUpdateTask createUpdateTask(StructureInfo structureInfo, String source) {
		return new StructureUpdateTask(structureInfo) {
			@Override
			protected SourceFileStructure createNewData() {
				SourceRange sr = new SourceRange(0, source.length());
				StructureElement element = new StructureElement("Outline_Not_Supported", sr, sr, 
					StructureElementKind.MODULEDEC, new ElementAttributes(null), null, null);
				Location fileLocation = structureInfo.getLocation();
				return new SourceFileStructure(fileLocation, new ArrayList2<StructureElement>(element), null);
			}
		};
	}
	
}