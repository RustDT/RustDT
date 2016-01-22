/*******************************************************************************
 * Copyright (c) 2014, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.editor;

import melnorme.lang.ide.ui.EditorSettings_Actual;
import melnorme.lang.ide.ui.LangUIPlugin;
import melnorme.lang.ide.ui.editor.structure.AbstractLangStructureEditor;
import melnorme.lang.ide.ui.editor.structure.LangOutlinePage;
import melnorme.lang.ide.ui.editor.structure.StructureElementContentProvider;
import melnorme.lang.ide.ui.editor.text.LangPairMatcher;
import melnorme.lang.ide.ui.text.AbstractLangSourceViewerConfiguration;
import melnorme.lang.ide.ui.text.LangSourceViewerConfiguration;
import melnorme.lang.tooling.structure.IStructureElementContainer;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.lang.tooling.structure.StructureElementKind;
import melnorme.util.swt.jface.text.ColorManager2;

public class RustEditor extends AbstractLangStructureEditor {
	
	@Override
	protected LangPairMatcher init_createBracketMatcher() {
		return new LangPairMatcher("{}[]()".toCharArray());
	}
	
	@Override
	protected AbstractLangSourceViewerConfiguration createSourceViewerConfiguration() {
		ColorManager2 colorManager = LangUIPlugin.getInstance().getColorManager();
		return new LangSourceViewerConfiguration(getPreferenceStore(), colorManager, this, 
			EditorSettings_Actual.getStylingPreferences());
	}
	
	@Override
	protected LangOutlinePage init_createOutlinePage() {
		return new LangOutlinePage(this) {
			@Override
			protected StructureElementContentProvider createContentProvider() {
				return new SpecializedStructureElementContentProvider();
			}
		};
	}
	
	public class SpecializedStructureElementContentProvider extends StructureElementContentProvider {
		@Override
		protected Object[] getChildren(IStructureElementContainer elementContainer) {
			if(elementContainer instanceof StructureElement) {
				StructureElement structureElement = (StructureElement) elementContainer;
				
				if(structureElement.getKind() == StructureElementKind.ENUM) {
					return null; // don't provide enum variants 
				}
				
				if(structureElement.getKind() == StructureElementKind.FUNCTION
						|| structureElement.getKind() == StructureElementKind.USE_GROUP) {
					return null; // don't provide children, simplify structure 
				}
			}
			return super.getChildren(elementContainer);
		}
	}
	
}