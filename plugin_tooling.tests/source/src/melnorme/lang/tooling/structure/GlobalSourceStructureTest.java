package melnorme.lang.tooling.structure;

import java.util.stream.Collectors;

import org.junit.Test;

import melnorme.lang.tooling.ast.SourceRange;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.collections.Indexable;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.tests.CommonTest;

public class GlobalSourceStructureTest extends CommonTest {
	private final GlobalSourceStructure sourceStructure = new GlobalSourceStructure();
	
	@Test
	public void initialSourceStructureIsEmpty() throws Exception {
		assertIndexContainsNames("");
	}
	
	@Test
	public void emptyListsAreNotAdded() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/path"), ArrayList2.create()));
		assertIndexContainsNames("");
	}
	
	@Test
	public void nonEmptyListsAreAdded() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/path"), ArrayList2.create(element("a"))));
		assertIndexContainsNames("a");
	}
	
	@Test
	public void emptyStructuresRemoveAlreadyAddedStructures() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/path"), ArrayList2.create(element("a"))));
		sourceStructure.updateIndex(structure(Location.create("/test/path"), ArrayList2.create()));
		assertIndexContainsNames("");
	}
	
	@Test
	public void nonEmptyStructuresReplaceAlreadyAddedStructures() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/path"), ArrayList2.create(element("a"))));
		sourceStructure.updateIndex(structure(Location.create("/test/path"), ArrayList2.create(element("b"))));
		assertIndexContainsNames("b");
	}
	
	@Test
	public void emptyStructuresDoNotRemoveStructuresFromDifferentPath() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/a"), ArrayList2.create(element("a"))));
		sourceStructure.updateIndex(structure(Location.create("/test/b"), ArrayList2.create()));
		assertIndexContainsNames("a");
	}
	
	@Test
	public void nonEmptyStructuresDoNotReplaceStructuresFromDifferentPath() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/a"), ArrayList2.create(element("a"))));
		sourceStructure.updateIndex(structure(Location.create("/test/b"), ArrayList2.create(element("b"))));
		assertIndexContainsNames("ab");
		sourceStructure.updateIndex(structure(Location.create("/test/c"), ArrayList2.create(element("c"))));
		assertIndexContainsNames("abc");
		sourceStructure.updateIndex(structure(Location.create("/test/d"), ArrayList2.create(element("d"))));
		assertIndexContainsNames("abcd");
	}
	
	@Test
	public void sourceStructureIsOrderedByPath() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/a"), ArrayList2.create(element("a"))));
		sourceStructure.updateIndex(structure(Location.create("/test/d"), ArrayList2.create(element("d"))));
		sourceStructure.updateIndex(structure(Location.create("/test/b"), ArrayList2.create(element("b"))));
		sourceStructure.updateIndex(structure(Location.create("/test/c"), ArrayList2.create(element("c"))));
		sourceStructure.updateIndex(structure(Location.create("/test/f"), ArrayList2.create(element("f"))));
		sourceStructure.updateIndex(structure(Location.create("/test/e"), ArrayList2.create(element("e"))));
		assertIndexContainsNames("abcdef");
	}
	
	@Test
	public void sourceStructureIsOrderedByElementName() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/a"),
			ArrayList2.create(element("a"), element("d"), element("b"), element("c"), element("f"), element("e"))));
		assertIndexContainsNames("abcdef");
	}
	
	@Test
	public void sourceStructureIsOrderedByOffset() throws Exception {
		sourceStructure.updateIndex(
			structure(Location.create("/test/a"), ArrayList2.create(elementWithOffset(1), elementWithOffset(4),
				elementWithOffset(2), elementWithOffset(3), elementWithOffset(6), elementWithOffset(5))));
		assertIndexContainsOffsets("123456");
	}
	
	@Test
	public void sourceStructureForASingleFileCannotContainTheSameNameAndRangeTwice() throws Exception {
		sourceStructure.updateIndex(
			structure(Location.create("/test/a"), ArrayList2.create(element("a"), element("a"), element("b"))));
		assertIndexContainsNames("ab");
	}
	
	@Test
	public void sourceStructureIsFlattened() throws Exception {
		sourceStructure.updateIndex(structure(Location.create("/test/a"),
			ArrayList2.create(element("a"), element("b", element("c")), element("d"))));
		assertIndexContainsNames("abcd");
	}
	
	private static SourceFileStructure structure(Location location, ArrayList2<StructureElement> children)
		throws CommonException {
		return new SourceFileStructure(location, children, Indexable.EMPTY_INDEXABLE);
	}
	
	private static StructureElement element(String name, StructureElement... children) {
		return new StructureElement(name, null, new SourceRange(0, 0), StructureElementKind.UNKNOWN, null, null,
			ArrayList2.create(children));
	}
	
	private static StructureElement elementWithOffset(int offset, StructureElement... children) {
		return new StructureElement("", null, new SourceRange(offset, 0), StructureElementKind.UNKNOWN, null, null,
			ArrayList2.create(children));
	}
	
	private void assertIndexContainsNames(String expectedStructure) {
		String structure = sourceStructure.getGlobalSourceStructure().stream().map(StructureElement::getName)
			.collect(Collectors.joining());
		
		assertEquals(structure, expectedStructure);
	}
	
	private void assertIndexContainsOffsets(String expectedLocations) {
		String structure = sourceStructure.getGlobalSourceStructure().stream()
			.map(structureElement -> Integer.toString(structureElement.getSourceRange().getOffset()))
			.collect(Collectors.joining());
		
		assertEquals(structure, expectedLocations);
	}
}
