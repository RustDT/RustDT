package melnorme.lang.ide.core.engine;

import java.util.Optional;

import melnorme.lang.tooling.structure.SourceFileStructure;
import melnorme.lang.utils.concurrency.ConcurrentlyDerivedData;
import melnorme.utilbox.core.fntypes.CommonResult;

public abstract class StructureResult<SELF> extends ConcurrentlyDerivedData<CommonResult<SourceFileStructure>, SELF> {
	public Optional<SourceFileStructure> getStructure() {
		return Optional.ofNullable(getStoredData())
			.map(CommonResult::getOrNull);
	}
}
