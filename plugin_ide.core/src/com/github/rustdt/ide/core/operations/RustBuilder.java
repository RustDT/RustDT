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
package com.github.rustdt.ide.core.operations;

import java.text.MessageFormat;

import melnorme.lang.ide.core.operations.LangProjectBuilderExt;
import melnorme.lang.ide.core.operations.SDKLocationValidator;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.data.LocationValidator;
import melnorme.lang.tooling.ops.ToolSourceError;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

import org.eclipse.core.runtime.CoreException;

/**
 * Rust builder, using Cargo.
 */
public class RustBuilder extends LangProjectBuilderExt {
	
	public static class RustSDKLocationValidator extends SDKLocationValidator {
		@Override
		protected String getSDKExecutable_append() {
			return "bin/cargo"; 
		}
		
		@Override
		protected String getSDKExecutableErrorMessage(Location exeLocation) {
			return MessageFormat.format("Cargo executable not found at Rust SDK location (`{0}`). ", exeLocation);
		}
	}
	
	public RustBuilder() {
	}
	
	@Override
	protected LocationValidator getSDKLocationValidator() {
		return new RustSDKLocationValidator();
	}
	
	@Override
	protected ProcessBuilder createBuildPB() throws CoreException {
		return createSDKProcessBuilder("build");
	}
	
	@Override
	protected void processBuildResult(ExternalProcessResult buildAllResult) throws CoreException {
		ArrayList2<ToolSourceError> buildErrors = new ArrayList2<>(); // TODO: process errors from result
		
		addErrorMarkers(buildErrors, ResourceUtils.getProjectLocation(getProject()));
	}
	
	@Override
	protected ProcessBuilder createCleanPB() throws CoreException {
		return createSDKProcessBuilder("clean");
	}
	
}