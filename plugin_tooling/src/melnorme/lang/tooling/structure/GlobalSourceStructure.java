package melnorme.lang.tooling.structure;

import static java.util.Comparator.comparing;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import melnorme.utilbox.misc.Location;

public class GlobalSourceStructure {
	private static final EnumSet<StructureElementKind> HIDDEN_ELEMENT_KINDS = EnumSet.of(
		StructureElementKind.EXTERN_CRATE, StructureElementKind.USE_GROUP, StructureElementKind.USE,
		StructureElementKind.VAR);
	
	private final SortedMap<Location, SortedSet<StructureElement>> index = new TreeMap<>(comparing(Location::toPath));
	
	public synchronized void updateIndex(SourceFileStructure fileStructure) {
		Optional<Location> location = fileStructure.getLocation();
		assertTrue(location.isPresent(), "Location is missing");
		
		if(fileStructure.getChildren().isEmpty()) {
			removeFromIndex(location.get());
		} else {
			putToIndex(fileStructure, location.get());
		}
	}
	
	private void removeFromIndex(Location location) {
		index.remove(location);
	}
	
	private void putToIndex(SourceFileStructure fileStructure, Location location) {
		SortedSet<StructureElement> elementsAtLocation =
			new TreeSet<>(comparing(StructureElement::getName).thenComparing(StructureElement::getSourceRange));
		
		fileStructure.visitSubTree(el -> {
			if(!HIDDEN_ELEMENT_KINDS.contains(el.getKind())) {
				elementsAtLocation.add(el);
			}
		});
		
		index.put(location, elementsAtLocation);
	}
	
	public synchronized List<StructureElement> getGlobalSourceStructure() {
		return index.values()
			.stream()
			.flatMap(Set::stream)
			.collect(Collectors.toList());
	};
}
