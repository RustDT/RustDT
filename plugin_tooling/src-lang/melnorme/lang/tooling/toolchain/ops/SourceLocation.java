/*******************************************************************************
 * Copyright (c) 2014, Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.tooling.toolchain.ops;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.util.Optional;
import java.util.function.BiFunction;

import melnorme.lang.tooling.ast.SourceRange;
import melnorme.utilbox.misc.Location;

public class SourceLocation {
	
	protected final Location fileLocation;
	protected final Optional<SourceRange> sourceRange;
	protected int oneBasedLineNumber;
	protected int oneBasedColNumber;
	
	private SourceLocation(Location fileLocation, Optional<SourceRange> sourceRange, int oneBasedLineNumber,
		int oneBasedColNumber) {
		this.fileLocation = assertNotNull(fileLocation);
		this.sourceRange = sourceRange;
		this.oneBasedLineNumber = oneBasedLineNumber;
		this.oneBasedColNumber = oneBasedColNumber;
	}
	
	public static SourceLocation forSourceRange(Location location, SourceRange sourceRange) {
		return new SourceLocation(location, Optional.of(sourceRange), 0, 0);
	}
	
	public static SourceLocation forOneBasedLineAndColNumber(Location fileLocation, int oneBasedLineNumber,
		int oneBasedColNumber) {
		assertTrue(oneBasedLineNumber > 0);
		assertTrue(oneBasedColNumber > 0);
		return new SourceLocation(fileLocation, Optional.empty(), oneBasedLineNumber, oneBasedColNumber);
	}
	
	public Location getFileLocation() {
		return fileLocation;
	}
	
	public SourceRange getSourceRange(BiFunction<Integer, Integer, Integer> linesColsToAbsolutePositionMapper) {
		return sourceRange.isPresent() ? sourceRange.get()
			: new SourceRange(linesColsToAbsolutePositionMapper.apply(oneBasedLineNumber, oneBasedColNumber), 0);
	}
}