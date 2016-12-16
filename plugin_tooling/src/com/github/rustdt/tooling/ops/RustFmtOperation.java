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

import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.toolchain.ops.AbstractToolOperation;
import melnorme.lang.tooling.toolchain.ops.IToolOperationService;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.tooling.toolchain.ops.SourceOpContext;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public class RustFmtOperation implements AbstractToolOperation<String> {
		
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
	
		ArrayList2<String> cmdLine = ArrayList2.create(rustFmt.toString());
		
//		cmdLine.add("--write-mode=diff");
		cmdLine.add("--skip-children");
		
		ProcessBuilder pb = new ProcessBuilder(cmdLine);
		// set directory, workaround for bug: https://github.com/rust-lang-nursery/rustfmt/issues/562
		// Also, it make rustfmt look for the rustfmt.toml config file in folders parent chain
		pb.directory(sourceOpContext.getFileLocation().getParent().toFile());
		
		String input = sourceOpContext.getSource();
		
		return handleResult(toolOpService.runProcess(pb, input, om));
	}
	
	protected String handleResult(ExternalProcessResult result) throws CommonException, OperationSoftFailure {
		int exitValue = result.exitValue;
		
		if(exitValue == 3) {
			// Some warnings were generated, but `rustfmt` did complete, so use the result
			// TODO: we could display the warnings in a non-modal notification UI
			// or even as warning markers?
		} else 
		if(exitValue != 0) {
			String msg = "`rustfmt` did not complete successfully, exit code: " + exitValue + "\n" + 
					result.getStdErrBytes().toUtf8String();
			throw new OperationSoftFailure(msg);
		}
		
		// formatted file is in stdout
		return result.getStdOutBytes().toUtf8String();
	}
}