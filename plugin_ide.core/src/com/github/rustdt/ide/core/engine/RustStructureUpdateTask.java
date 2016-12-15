package com.github.rustdt.ide.core.engine;

import com.github.rustdt.ide.core.operations.RustParseDescribeLauncher;
import com.github.rustdt.tooling.ops.RustParseDescribeParser;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.engine.StructureResult;
import melnorme.lang.ide.core.engine.StructureUpdateTask;
import melnorme.lang.tooling.structure.SourceFileStructure;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;

abstract class RustStructureUpdateTask extends StructureUpdateTask {
	RustStructureUpdateTask(StructureResult<?> derivedData, Location location) {
		super(derivedData, location);
	}
	
	@Override
	protected void handleRuntimeException(RuntimeException e) {
		LangCore.logInternalError(e);
	}
	
	interface SourceProvider {
		String provideSource() throws CommonException;
	}
	
	static class RustStructureSourceTouchedTask extends RustStructureUpdateTask {
		private final SourceProvider sourceProvider;
		
		RustStructureSourceTouchedTask(StructureResult<?> structureResult, Location location, SourceProvider sourceProvider) {
			super(structureResult, location);
			this.sourceProvider = sourceProvider;
		}
		
		@Override
		protected SourceFileStructure doCreateNewData() throws OperationCancellation, CommonException {
			String source = sourceProvider.provideSource();
			RustParseDescribeLauncher parseDescribeLauncher = new RustParseDescribeLauncher(
				LangCore.getToolManager(), this::isCancelled);
			String parseDescribeStdout = parseDescribeLauncher.getDescribeOutput(source, location);
			
			RustParseDescribeParser parseDescribeParser = new RustParseDescribeParser(location, source);
			return parseDescribeParser.parse(parseDescribeStdout);
		}
	}
	
	static class RustStructureFileRemovedTask extends RustStructureUpdateTask {
		RustStructureFileRemovedTask(StructureResult<?> structureResult, Location location) {
			super(structureResult, location);
		}
		
		@Override
		protected SourceFileStructure doCreateNewData() {
			return new SourceFileStructure(location, Indexable.EMPTY_INDEXABLE, Indexable.EMPTY_INDEXABLE);
		}
	}
}