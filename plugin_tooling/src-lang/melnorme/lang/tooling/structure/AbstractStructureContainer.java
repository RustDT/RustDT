/*******************************************************************************
 * Copyright (c) 2015, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.tooling.structure;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import static melnorme.utilbox.core.CoreUtil.areEqual;
import static melnorme.utilbox.core.CoreUtil.nullToEmpty;

import java.util.function.Consumer;

import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.misc.HashcodeUtil;

public abstract class AbstractStructureContainer implements IStructureElementContainer {
	
	protected final Indexable<StructureElement> children;
	
	public AbstractStructureContainer(Indexable<? extends StructureElement> _children) {
		this.children = nullToEmpty(_children).upcastTypeParameter();
		
		for(StructureElement child : children) {
			child.setParent(this);
		}
		
		_invariant();
	}
	
	protected void _invariant() {
		for(StructureElement element : getChildren()) {
			assertTrue(element.getParent() == this);
		}
	}
	
	@Override
	public Indexable<StructureElement> getChildren() {
		return children;
	}
	
	public Indexable<StructureElement> cloneSubTree() {
		ArrayList2<StructureElement> clonedElements = new ArrayList2<>(children.size());
		for(StructureElement child : children) {
			clonedElements.add(child.cloneTree());
		}
		return clonedElements;
	}
	
	public void visitSubTree(Consumer<StructureElement> visitor) {
		for(StructureElement child : getChildren()) {
			child.visitTree(visitor);
		}
	}
	
	/* ----------------- ----------------- */
	
	public static class SimpleStructureContainer extends AbstractStructureContainer {
		
		public SimpleStructureContainer(Indexable<? extends StructureElement> children) {
			super(children);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof SimpleStructureContainer)) return false;
			
			SimpleStructureContainer other = (SimpleStructureContainer) obj;
			
			return areEqual(children, other.children);
		}
		
		@Override
		public int hashCode() {
			return HashcodeUtil.combinedHashCode(children);
		}
		
	}
	
}