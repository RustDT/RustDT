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
package com.github.rustdt.ide.ui;

import melnorme.lang.ide.ui.LangElementImages;
import melnorme.lang.ide.ui.LangImages;
import melnorme.lang.ide.ui.utils.PluginImagesHelper.ImageHandle;

public interface RustElementImages extends LangElementImages {
	
	String CAT = "language_elements";
	
	ImageHandle T_IMPL = LangImages.createManaged(CAT, "t_impl.png");
	ImageHandle T_TRAIT = LangImages.createManaged(CAT, "t_trait.png");
	ImageHandle T_TYPE = LangImages.createManaged(CAT, "t_type.png");
	
	ImageHandle T_ENUM_VARIANT = LangImages.createManaged(CAT, "t_enum_variant.png");
	
}