/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling.ops;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.nio.file.Path;

import melnorme.lang.tooling.ToolingMessages;
import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.toolchain.ops.AbstractToolOperation;
import melnorme.lang.tooling.toolchain.ops.IToolOperationService;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.tooling.toolchain.ops.SourceOpContext;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RustFmtOperation implements AbstractToolOperation<String> {
		
	protected boolean rustfmtFailureAsHardFailure = true;
	
	protected final SourceOpContext sourceOpContext;
	protected final IToolOperationService toolOpService;
	protected final Path rustFmt;
	
	public RustFmtOperation(SourceOpContext sourceOpContext, IToolOperationService toolOpService, Path rustFmt) {
		this.sourceOpContext = assertNotNull(sourceOpContext);
		this.toolOpService = assertNotNull(toolOpService);
		this.rustFmt = assertNotNull(rustFmt);
	}
	
	@Override
	public String executeToolOperation(IOperationMonitor om) 
			throws CommonException, OperationCancellation, OperationSoftFailure {
	
		ArrayList2<String> cmdLine = new ArrayList2<>(rustFmt.toString());
		
//		cmdLine.add("--write-mode=diff");
		cmdLine.add("--skip-children");
		
		ProcessBuilder pb = new ProcessBuilder(cmdLine);
		// set directory, workaround for bug: https://github.com/rust-lang-nursery/rustfmt/issues/562
		// Also, it make rustfmt look for the rustfmt.toml config file in folders parent chain
		pb.directory(sourceOpContext.getFileLocation().getParent().toFile());
		
		String input = sourceOpContext.getSource();
		ExternalProcessResult result = toolOpService.runProcess(pb, input, om);
		int exitValue = result.exitValue;
		
		if(exitValue != 0) {
			String stdErr = result.getStdErrBytes().toUtf8String();
			String firstStderrLine = StringUtil.splitString(stdErr, '\n')[0].trim();
			
			String errorMessage = ToolingMessages.PROCESS_CompletedWithNonZeroValue("rustfmt", exitValue) + "\n" +
					firstStderrLine;
			if(rustfmtFailureAsHardFailure) {
				throw new CommonException(errorMessage);
			} else {
				throw new OperationSoftFailure(errorMessage);
			}
		}
		
		// formatted file is in stdout
		return result.getStdOutBytes().toUtf8String();
	}
}