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
package com.github.rustdt.tooling.ops;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import melnorme.lang.tooling.common.ops.IOperationMonitor;
import melnorme.lang.tooling.toolchain.ops.AbstractToolInvocationOperation;
import melnorme.lang.tooling.toolchain.ops.IToolOperationService;
import melnorme.lang.tooling.toolchain.ops.OperationSoftFailure;
import melnorme.lang.tooling.toolchain.ops.SourceOpContext;
import melnorme.lang.utils.validators.LocationOrSinglePathValidator;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.fields.validation.ValidatedValueSource;
import melnorme.utilbox.misc.FileUtil;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.status.Severity;
import melnorme.utilbox.status.StatusException;

public abstract class RacerOperation<RESULTDATA> extends AbstractToolInvocationOperation<RESULTDATA> {
	
	protected final ValidatedValueSource<Path> racerPath;
	protected final ValidatedValueSource<Location> sdkSrcLocation;
	protected final SourceOpContext sourceOpContext;
	protected final String source;
	protected final boolean useSubstituteFile;
	
	protected Location substituteFile;
	
	public RacerOperation(IToolOperationService opHelper,
			ValidatedValueSource<Path> racerPath, ValidatedValueSource<Location> sdkSrcLocation,
			SourceOpContext sourceOpContext) {
		super(opHelper, "NOT_USED");
		this.racerPath = racerPath;
		this.sdkSrcLocation = sdkSrcLocation;
		this.sourceOpContext = assertNotNull(sourceOpContext);
		this.source = sourceOpContext.getSource();
		this.useSubstituteFile = sourceOpContext.isDocumentDirty();
	}
	
	@Override
	protected String getToolName() throws CommonException {
		return "Racer";
	}
	
	protected static ArrayList2<String> getArguments(String opName, int line_0, int col_0, Location fileLocation) {
		ArrayList2<String> arguments = new ArrayList2<String>(opName);
		arguments.add(Integer.toString(line_0 + 1)); // use one-based index
		arguments.add(Integer.toString(col_0)); // But this one is zero-based index
		arguments.add(fileLocation.toPathString());
		return arguments;
	}
	
	@Override
	public RESULTDATA executeToolOperation(IOperationMonitor om)
			throws CommonException, OperationCancellation, OperationSoftFailure {
		
		if(useSubstituteFile()) {
			assertNotNull(source);
			createSubstituteFile(source);
		}
		
		RESULTDATA result = super.executeToolOperation(om);
		
		if(useSubstituteFile()) {
			substituteFile.toFile().delete();
		}
		
		return result;
	}
	
	protected void createSubstituteFile(String source) throws CommonException {
		try {
			substituteFile = Location.create_fromValid(Files.createTempFile("RustDT-", "-racer_substitute_file.rs"));
			FileUtil.writeStringToFile(substituteFile.toFile(), source, StringUtil.UTF8);
		} catch(IOException e) {
			throw new CommonException("Error creating substitute file for Racer: ", e);
		}
	}
	
	protected boolean useSubstituteFile() {
		return useSubstituteFile;
	}
	
	@Override
	protected ProcessBuilder createProcessBuilder() throws CommonException {
		String toolExePath = racerPath.getValidatedValue().toString();
		String rustSrcPath = sdkSrcLocation.getValidatedValue().toString();
		
		ArrayList2<String> cmdLine = new ArrayList2<String>(toolExePath);
		
		cmdLine.addAll2(getRacerArguments());
		
		if(substituteFile != null) {
			cmdLine.add(substituteFile.toPathString());
		}
		
		ProcessBuilder pb = new ProcessBuilder(cmdLine);
		
		pb.environment().put("RUST_SRC_PATH", rustSrcPath);
		return pb;
	}
	
	protected abstract Indexable<String> getRacerArguments() throws CommonException;
	
	public static class RustRacerLocationValidator extends LocationOrSinglePathValidator {
		
		public RustRacerLocationValidator() {
			super("Racer executable:");
			fileOnly = true;
		}
		
	}
	
	/* -----------------  ----------------- */
	
	protected RacerCompletionOutputParser createRacerOutputParser(int offset) {
		RacerCompletionOutputParser parser = new RacerCompletionOutputParser(offset) {
			@Override
			protected void handleUnknownLineFormat(CommonException ce) throws CommonException {
				opHelper.logStatus(ce.toStatusException(Severity.WARNING));
			}
			
			@Override
			protected void handleInvalidMatchKindString(String matchKindString) throws CommonException {
				opHelper.logStatus(new StatusException(Severity.WARNING,
					"Unknown Match Kind: " + matchKindString));
			}
		};
		return parser;
	}
	
}