/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.ide.ui.navigator;

import static melnorme.lang.ide.ui.views.StylerHelpers.fgColor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.RGB;

import com.github.rustdt.ide.core.cargomodel.RustBundleInfo;
import com.github.rustdt.tooling.cargo.CargoManifestParser;

import melnorme.lang.ide.core.LangCore_Actual;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.ide.ui.LangImages;
import melnorme.lang.ide.ui.navigator.NavigatorElementsSwitcher;
import melnorme.lang.ide.ui.views.LangNavigatorLabelProvider;

public class RustNavigatorLabelProvider extends LangNavigatorLabelProvider implements IStyledLabelProvider {
	
	public static interface RustNavigatorLabelElementsSwitcher<RET> extends NavigatorElementsSwitcher<RET> {
		@Override
		default RET visitOther(Object element) {
			if(element instanceof IFile) {
				IFile file = (IFile) element;
				
				if(file.getProjectRelativePath().equals(ResourceUtils.epath(CargoManifestParser.MANIFEST_FILENAME))) {
					return visitManifestFile(file);
				}
			}
			
			if(element instanceof IFolder) {
				IFolder folder = (IFolder) element;
				if(folder.getProjectRelativePath().equals(new Path("src"))) {
					return visitSourceFolder(folder);
				}
				if(folder.getProjectRelativePath().equals(new Path("tests"))) {
					return visitTestsSourceFolder(folder);
				}
				if(folder.getProjectRelativePath().equals(new Path("target"))) {
					return visitBuildOutpuFolder(folder);
				}
			}
			
			return null;
		}
		
		public abstract RET visitManifestFile(IFile element);
		
		public abstract RET visitSourceFolder(IFolder element);
		
		public abstract RET visitTestsSourceFolder(IFolder element);
		
		public abstract RET visitBuildOutpuFolder(IFolder element);
		
	}
	
	@Override
	protected DefaultGetStyledTextSwitcher getStyledText_switcher() {
		return new RustNavigatorStyledTextSwitcher();
	}
	
	@Override
	protected DefaultGetImageSwitcher getBaseImage_switcher() {
		return new RustNavigatorImageSwitchers();
	}
	
	protected static final RGB ANNOTATION_FG = new RGB(120, 120, 200);
	
	protected class RustNavigatorStyledTextSwitcher extends DefaultGetStyledTextSwitcher
			implements RustNavigatorLabelElementsSwitcher<StyledString> {
			
		@Override
		public StyledString visitManifestFile(IFile element) {
			RustBundleInfo bundleInfo = LangCore_Actual.getBundleModel().getProjectInfo(element.getProject());
			if(bundleInfo == null) {
				return null;
			}
			String bundleVersion = bundleInfo.getManifest().getVersion();
			
			StyledString baseString = new StyledString(element.getName());
			String versionString = bundleVersion == null ? "?" : bundleVersion;
			String crateInfo = " [" + bundleInfo.getCrateName() + " " + versionString + "]";
			return baseString.append(crateInfo, fgColor(ANNOTATION_FG));
		}
		
		@Override
		public StyledString visitSourceFolder(IFolder element) {
			return null;
		}
		
		@Override
		public StyledString visitTestsSourceFolder(IFolder element) {
			return null;
		}
		
		@Override
		public StyledString visitBuildOutpuFolder(IFolder element) {
			return null;
		}
		
	}
	
	protected class RustNavigatorImageSwitchers extends DefaultGetImageSwitcher
			implements RustNavigatorLabelElementsSwitcher<ImageDescriptor> {
			
		@Override
		public ImageDescriptor visitManifestFile(IFile element) {
			return LangImages.NAV_PackageManifest;
		}
		
		@Override
		public ImageDescriptor visitSourceFolder(IFolder element) {
			return LangImages.NAV_SourceFolder;
		}
		
		@Override
		public ImageDescriptor visitTestsSourceFolder(IFolder element) {
			return LangImages.NAV_SourceFolderTests;
		}
		
		@Override
		public ImageDescriptor visitBuildOutpuFolder(IFolder element) {
			return LangImages.NAV_OutputFolder;
		}
		
	}
	
}