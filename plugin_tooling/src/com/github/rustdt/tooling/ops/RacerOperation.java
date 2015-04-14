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
package com.github.rustdt.tooling.ops;

import java.text.MessageFormat;

import melnorme.lang.tooling.data.SDKLocationValidator;
import melnorme.lang.tooling.ops.AbstractToolOperation;
import melnorme.lang.tooling.ops.IProcessRunner;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public abstract class RacerOperation extends AbstractToolOperation {
	
	protected final String toolPath;
	protected final String rustPath;
	protected final ArrayList2<String> arguments;
	
	protected String input = "";
	
	public RacerOperation(IProcessRunner processRunner, String toolPath, String rustPath, 
			ArrayList2<String> arguments) {
		super(processRunner);
		this.toolPath = toolPath;
		this.rustPath = rustPath;
		this.arguments = arguments;
	}
	
	public ExternalProcessResult execute() throws CommonException, OperationCancellation {
		
		String toolExePath = new RustRacerLocationValidator().getValidatedField(toolPath).toPathString();
		String rustSrcPath = new RustSDKLocationValidator().getValidatedField(rustPath).resolve_fromValid("../../src").
				toPathString();
		
		ArrayList2<String> cmdLine = new ArrayList2<String>(toolExePath);
		
		cmdLine.addAll(arguments);
		
		ProcessBuilder pb = new ProcessBuilder(cmdLine);
		
		pb.environment().put("RUST_SRC_PATH", rustSrcPath);
		
		return processRunner.runProcess(pb, input);
	}
	
	public static class RustRacerLocationValidator extends SDKLocationValidator {
		
		public RustRacerLocationValidator() {
			super("Racer installation:");
		}
		
		@Override
		protected String getSDKExecutable_append() {
			return "racer"; 
		}
		
		@Override
		protected String getSDKExecutableErrorMessage(Location exeLocation) {
			return MessageFormat.format("Racer executable not found at location (`{0}`). ", exeLocation);
		}
	}
	
}