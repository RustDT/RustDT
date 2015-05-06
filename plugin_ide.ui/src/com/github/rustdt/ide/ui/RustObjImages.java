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
package com.github.rustdt.ide.ui;

import melnorme.lang.ide.ui.LangElementImages;
import melnorme.lang.ide.ui.LangImages;
import melnorme.lang.ide.ui.utils.PluginImagesHelper.ImageHandle;

public interface RustObjImages extends LangElementImages {
	
	ImageHandle T_TRAIT = LangImages.createManaged(CAT_OBJ, "t_trait.png");
	ImageHandle T_TYPE = LangImages.createManaged(CAT_OBJ, "t_type.png");
	
}