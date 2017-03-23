package com.github.rustdt.ide.core.engine;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;

import com.github.rustdt.ide.core.engine.RustStructureUpdateTask.RustStructureFileRemovedTask;
import com.github.rustdt.ide.core.engine.RustStructureUpdateTask.RustStructureSourceTouchedTask;
import com.github.rustdt.ide.core.engine.RustStructureUpdateTask.SourceProvider;

import melnorme.lang.ide.core.LangCore;
import melnorme.lang.ide.core.engine.IndexManager;
import melnorme.lang.ide.core.engine.StructureResult;
import melnorme.lang.ide.core.utils.ResourceUtils;
import melnorme.lang.tooling.structure.GlobalSourceStructure;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.utilbox.misc.FileUtil;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.misc.StringUtil;

public class RustIndexManager extends IndexManager {
	private final GlobalSourceStructure sourceStructure = new GlobalSourceStructure();
	
	private final Map<Location, StructureInfo> startedIndexUpdates = new ConcurrentHashMap<>();
	
	private final IResourceChangeListener resourcesChanged = this::processResourceChangeEvent;
	
	public RustIndexManager() {
		evaluateGlobalSourceStructure();
		listenToUpdatesOfGlobalSourceStructure();
		asOwner().bind(this::stopListeningToUpdatesOfGlobalSourceStructure);
	}
	
	private void evaluateGlobalSourceStructure() {
		try {
			ResourcesPlugin.getWorkspace().getRoot().accept(RustIndexManager.this::addResource);
		} catch(Exception e) {
			LangCore.logError("Could not initialize Rust type index", e);
		}
	}
	
	private boolean addResource(IResource resource) {
		convertToRustFileLocation(resource).ifPresent(this::enqueueFileTouchedTask);
		return true;
	}
	
	private void listenToUpdatesOfGlobalSourceStructure() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourcesChanged, IResourceChangeEvent.POST_CHANGE);
	}
	
	private void stopListeningToUpdatesOfGlobalSourceStructure() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourcesChanged);
	}
	
	private void processResourceChangeEvent(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(this::applyResourceDelta);
		} catch(Exception e) {
			LangCore.logError("Could not update Rust type index", e);
		}
	}
	
	private boolean applyResourceDelta(IResourceDelta delta) {
		convertToRustFileLocation(delta.getResource()).ifPresent(location -> {
			switch(delta.getKind()) {
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				enqueueFileTouchedTask(location);
				break;
			case IResourceDelta.REMOVED:
				enqueueFileRemovedTask(location);
				break;
			}
		});
		return true;
	}
	
	private static Optional<Location> convertToRustFileLocation(IResource resource) {
		return Optional.of(resource)
			.filter(res -> res instanceof IFile)
			.filter(RustIndexManager::isValidRustPath)
			.map(ResourceUtils::getResourceLocation);
	}
	
	private static boolean isValidRustPath(IResource resource) {
		return resource.getFullPath().toString().endsWith(".rs");
	}
	
	private void enqueueFileRemovedTask(Location location) {
		StructureInfo structureInfo = getOrCreateStructureInfo(location);
		RustStructureUpdateTask indexUpdateTask = new RustStructureFileRemovedTask(structureInfo, location);
		
		structureInfo.setUpdateTask(indexUpdateTask);
		executor.submitTask(indexUpdateTask);
	}
	
	private void enqueueFileTouchedTask(Location location) {
		StructureInfo structureInfo = getOrCreateStructureInfo(location);
		
		SourceProvider sourceProvider = () -> FileUtil.readFileContents(location, StringUtil.UTF8);
		RustStructureUpdateTask indexUpdateTask =
			new RustStructureSourceTouchedTask(structureInfo, location, sourceProvider);
		
		structureInfo.setUpdateTask(indexUpdateTask);
		executor.submitTask(indexUpdateTask);
	}
	
	private StructureInfo getOrCreateStructureInfo(Location location) {
		StructureInfo structureInfo = startedIndexUpdates.get(location);
		if(structureInfo == null) {
			structureInfo = new StructureInfo();
			startedIndexUpdates.put(location, structureInfo);
		}
		return structureInfo;
	}
	
	private class StructureInfo extends StructureResult<StructureInfo> {
		@Override
		protected void doHandleDataChanged() {
			getStructure().ifPresent(
				structure -> {
					sourceStructure.updateIndex(structure);
					structure.getLocation().ifPresent(startedIndexUpdates::remove);
				});
		}
	}
	
	@Override
	public List<StructureElement> getGlobalSourceStructure() {
		return sourceStructure.getGlobalSourceStructure();
	}
}
