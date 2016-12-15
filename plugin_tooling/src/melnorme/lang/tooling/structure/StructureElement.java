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
package melnorme.lang.tooling.structure;

import java.util.function.Consumer;

import melnorme.lang.tooling.ElementAttributes;
import melnorme.lang.tooling.LANG_SPECIFIC;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.utilbox.collections.Indexable;

@LANG_SPECIFIC
public class StructureElement extends StructureElement_Default {
	
	public StructureElement(String name, SourceRange nameSourceRange, SourceRange sourceRange,
			StructureElementKind elementKind, ElementAttributes elementAttributes, String type,
			Indexable<StructureElement> children) {
		super(name, nameSourceRange, sourceRange, elementKind, elementAttributes, type, children);
	}
	
	public StructureElement cloneTree() {
		return cloneWithChildren(cloneSubTree());
	}
	
	public void visitTree(Consumer<StructureElement> visitor) {
		visitor.accept(this);
		visitSubTree(visitor);
	}
	
	private StructureElement cloneWithChildren(Indexable<StructureElement> children) {
		return new StructureElement(
			name, nameSourceRange2, sourceRange, elementKind, elementAttributes, type, children);
	}
}
