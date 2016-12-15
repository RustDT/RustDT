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
package melnorme.lang.ide.ui.views;

import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import melnorme.lang.ide.ui.LangImageProvider;
import melnorme.lang.ide.ui.LangImages;
import melnorme.lang.ide.ui.LangUIPlugin_Actual;
import melnorme.lang.tooling.ElementAttributes;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.util.swt.jface.resources.LangElementImageDescriptor;
import melnorme.utilbox.misc.Location;

public abstract class StructureElementLabelProvider extends AbstractLangLabelProvider {
	public enum AdditionalInfo {
		SIGNATURE, LOCATION
	}
	
	private final AdditionalInfo additionalInfo;
	
	public static DelegatingStyledCellLabelProvider createLangLabelProvider() {
		StructureElementLabelProvider labelProvider =
			LangUIPlugin_Actual.getStructureElementLabelProvider(AdditionalInfo.SIGNATURE);
		// We wrap the base LabelProvider with a DelegatingStyledCellLabelProvider because for some reason
		// that prevents flicker problems when changing selection in Windows classic themes
		// Might not be necessary in the future.
		return new DelegatingStyledCellLabelProvider(labelProvider);
	}
	
	/* -----------------  ----------------- */
	
	public StructureElementLabelProvider(AdditionalInfo additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
	
	@Override
	public StyledString getStyledText(Object element) {
		if(element instanceof StructureElement) {
			StructureElement structureElement = (StructureElement) element;
			return getStyledText(structureElement);
		}
		return null;
	}
	
	protected StyledString getStyledText(StructureElement structureElement) {
		StyledString styledString = new StyledString(structureElement.getName());
		
		if(additionalInfo == AdditionalInfo.SIGNATURE && structureElement.getType() != null) {
			String typeSuffix = getTypeDescriptionPrefix(structureElement) + structureElement.getType();
			styledString.append(typeSuffix, StyledString.DECORATIONS_STYLER);
		}
		Optional<Location> location = structureElement.getLocation();
		if(additionalInfo == AdditionalInfo.LOCATION && location.isPresent()) {
			styledString.append(" - ");
			styledString.append(getAsRelativePath(location.get()).toString(), StyledString.DECORATIONS_STYLER);
		}
		return styledString;
	}
	
	private IPath getAsRelativePath(Location location) {
		return new Path(location.toPathString()).makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation());
	}
	
	@SuppressWarnings("unused")
	protected String getTypeDescriptionPrefix(StructureElement structureElement) {
		return " : ";
	}
	
	@Override
	public Image getImage(Object element) {
		if(element instanceof StructureElement) {
			StructureElement structureElement = (StructureElement) element;
			
			return getImage(structureElement);
		}
		return null;
	}
	
	public Image getImage(StructureElement element) {
		ImageDescriptor imageDescriptor = getImageDescriptor(element);
		return LangImages.getImageDescriptorRegistry().get(imageDescriptor);
	}
	
	protected ImageDescriptor getImageDescriptor(StructureElement element) {
		ImageDescriptor baseImageDescriptor = getBaseImageDescriptor(element);
		return getElementImageDescriptor(baseImageDescriptor, element.getAttributes());
	}
	
	protected ImageDescriptor getBaseImageDescriptor(StructureElement structureElement) {
		return structureElement.getKind().switchOnKind(new LangImageProvider()).getDescriptor();
	}
	
	public LangElementImageDescriptor getElementImageDescriptor(ImageDescriptor baseImage,
			ElementAttributes elementAttributes) {
		return new LangElementImageDescriptor(baseImage, elementAttributes);
	}
	
}