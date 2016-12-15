package melnorme.lang.ide.core.engine;

import java.util.List;

import melnorme.lang.tooling.structure.StructureElement;

public abstract class IndexManager extends AbstractAgentManager {
	public abstract List<StructureElement> getGlobalSourceStructure();
}
