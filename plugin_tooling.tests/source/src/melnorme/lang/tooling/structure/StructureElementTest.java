package melnorme.lang.tooling.structure;

import org.junit.Test;

import melnorme.lang.tooling.ast.SourceRange;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.tests.CommonTest;

public class StructureElementTest extends CommonTest {
	@Test
	public void testVisitSubTree() {
		verifySubTreeVisit(container(), "");
		verifySubTreeVisit(container(element("a")), "a");
		verifySubTreeVisit(container(element("a"), element("b")), "ab");
		verifySubTreeVisit(container(element("a", element("b"))), "ab");
		verifySubTreeVisit(container(element("a"), element("b"), element("c")), "abc");
		verifySubTreeVisit(container(element("a", element("b")), element("c")), "abc");
		verifySubTreeVisit(container(element("a"), element("b", element("c"))), "abc");
		verifySubTreeVisit(container(element("a", element("b", element("c")))), "abc");
		
		verifySubTreeVisit(element("x"), "");
		verifySubTreeVisit(element("x", element("a")), "a");
		verifySubTreeVisit(element("x", element("a"), element("b")), "ab");
		verifySubTreeVisit(element("x", element("a", element("b"))), "ab");
		verifySubTreeVisit(element("x", element("a"), element("b"), element("c")), "abc");
		verifySubTreeVisit(element("x", element("a", element("b")), element("c")), "abc");
		verifySubTreeVisit(element("x", element("a"), element("b", element("c"))), "abc");
		verifySubTreeVisit(element("x", element("a", element("b", element("c")))), "abc");
	}
	
	private static void verifySubTreeVisit(AbstractStructureContainer structureToTest, String visited) {
		StringBuilder stringBuilder = new StringBuilder();
		structureToTest.visitSubTree(el -> stringBuilder.append(el.getName()));
		assertEquals(stringBuilder.toString(), visited);
	}
	
	@Test
	public void testVisitTree() {
		verifyTreeVisit(element("x"), "x");
		verifyTreeVisit(element("x", element("a")), "xa");
		verifyTreeVisit(element("x", element("a"), element("b")), "xab");
		verifyTreeVisit(element("x", element("a", element("b"))), "xab");
		verifyTreeVisit(element("x", element("a"), element("b"), element("c")), "xabc");
		verifyTreeVisit(element("x", element("a", element("b")), element("c")), "xabc");
		verifyTreeVisit(element("x", element("a"), element("b", element("c"))), "xabc");
		verifyTreeVisit(element("x", element("a", element("b", element("c")))), "xabc");
	}
	
	private static void verifyTreeVisit(StructureElement structureToTest, String visited) {
		StringBuilder stringBuilder = new StringBuilder();
		structureToTest.visitTree(el -> stringBuilder.append(el.getName()));
		assertEquals(stringBuilder.toString(), visited);
	}
	
	private static AbstractStructureContainer container(StructureElement... children) {
		return new AbstractStructureContainer(ArrayList2.create(children)) {
		};
	}
	
	private static StructureElement element(String name, StructureElement... children) {
		return new StructureElement(
			name, null, new SourceRange(0, 0), StructureElementKind.UNKNOWN, null, null, ArrayList2.create(children));
	}
	
}