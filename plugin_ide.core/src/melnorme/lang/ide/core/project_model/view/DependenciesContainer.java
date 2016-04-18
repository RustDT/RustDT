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
package melnorme.lang.ide.core.project_model.view;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;

import com.github.rustdt.tooling.cargo.CargoManifest;
import com.github.rustdt.tooling.cargo.CargoManifest.CrateDependencyRef;

import melnorme.lang.tooling.LANG_SPECIFIC;
import melnorme.lang.tooling.bundle.BundleInfo;
import melnorme.utilbox.misc.ArrayUtil;

@LANG_SPECIFIC
public class DependenciesContainer extends AbstractDependenciesContainer<BundleInfo> {
	
	public DependenciesContainer(BundleInfo bundleInfo, IProject project) {
		super(bundleInfo, project);
	}
	
	protected CargoManifest getBundleDesc() {
		return bundleInfo.getManifest();
	}
	
	@Override
	protected IBundleModelElement[] createChildren() {
		ArrayList<IBundleModelElement> newChildren = new ArrayList<>();
		
		for(CrateDependencyRef bundleRef : bundleInfo.getManifest().getDependencies()) {
			newChildren.add(new RawDependencyElement(this, bundleRef));
		}
		
		return ArrayUtil.createFrom(newChildren, IBundleModelElement.class);
	}
	
}