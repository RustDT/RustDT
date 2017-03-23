package melnorme.lang.ide.core.engine;

import java.util.Optional;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.engine.SourceModelManager.StructureInfo;
import melnorme.lang.tooling.structure.SourceFileStructure;
import melnorme.lang.utils.concurrency.ConcurrentlyDerivedData.DataUpdateTask;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.core.fntypes.CommonResult;
import melnorme.utilbox.misc.Location;

public abstract class StructureUpdateTask extends DataUpdateTask<CommonResult<SourceFileStructure>> {
	private final StructureResult<?> structureResult;
	protected final Location location;
	
	public StructureUpdateTask(StructureInfo structureInfo) {
		this(structureInfo, structureInfo.getKey2().getLocation());
	}
	
	public StructureUpdateTask(StructureResult<?> structureResult, Location location) {
		super(structureResult, location != null ? location.toString() : null);
		this.structureResult = structureResult;
		this.location = location;
	}
	
	
	@Override
	protected void handleRuntimeException(RuntimeException e) {
		LangCore.logInternalError(e);
	}
	
	@Override
	protected final CommonResult<SourceFileStructure> createNewData() throws OperationCancellation {
		try {
			SourceFileStructure newStructure = doCreateNewData();
			boolean keepPreviousStructure =
				!newStructure.getParserProblems().isEmpty() && newStructure.getChildren().isEmpty();
			if(keepPreviousStructure) {
				Optional<SourceFileStructure> previousStructure = structureResult.getStructure();
				newStructure = previousStructure.isPresent()
					? new SourceFileStructure(location, previousStructure.get().cloneSubTree(),
						newStructure.getParserProblems())
					: new SourceFileStructure(location, Indexable.EMPTY_INDEXABLE, Indexable.EMPTY_INDEXABLE);
			}
			return new CommonResult<>(newStructure);
		} catch(CommonException e) {
			return new CommonResult<>(null, e);
		}
	}
	
	protected abstract SourceFileStructure doCreateNewData() throws CommonException, OperationCancellation;
}