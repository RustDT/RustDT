package melnorme.lang.tooling.toolchain.ops;

import java.util.function.BiFunction;

import org.junit.Test;

import melnorme.lang.tooling.ast.SourceRange;
import melnorme.utilbox.misc.Location;
import melnorme.utilbox.tests.CommonTest;

public class SourceLocationTest extends CommonTest {
	@Test
	public void rangeBasedSourceLocationReturnsSourceRangeWithoutInvokingTheMapper() throws Exception {
		Location location = Location.create("/");
		SourceRange sourceRange = SourceRange.srStartToEnd(0, 100);
		SourceLocation sourceLocation = SourceLocation.forSourceRange(location, sourceRange);
		
		BiFunction<Integer, Integer, Integer> evilMapper = (a, b) -> {
			throw new RuntimeException();
		};
		
		assertEquals(sourceLocation.getSourceRange(evilMapper), sourceRange);
		assertEquals(sourceLocation.getFileLocation(), location);
	}
	
	@Test
	public void linesAndColsBasedSourceLocationReturnsSourceRangeEvaluatedByTheMapper() throws Exception {
		Location location = Location.create("/");
		SourceLocation sourceLocation = SourceLocation.forOneBasedLineAndColNumber(location, 3, 5);
		
		BiFunction<Integer, Integer, Integer> summarizingFakeMapper = (a, b) -> a + b;
		
		SourceRange expectedSourceRange = new SourceRange(8, 0);
		assertEquals(sourceLocation.getSourceRange(summarizingFakeMapper), expectedSourceRange);
		assertEquals(sourceLocation.getFileLocation(), location);
	}
}
