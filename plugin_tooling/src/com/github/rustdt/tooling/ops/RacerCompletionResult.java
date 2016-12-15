package com.github.rustdt.tooling.ops;

import melnorme.utilbox.misc.Location;

public class RacerCompletionResult {
	
	public final Location location;
	public final int oneBasedLineNumber;
	public final int zeroBasedColumnNumber;
	
	public RacerCompletionResult(Location location, int oneBasedLineNumber, int zeroBasedColumnNumber) {
		this.location = location;
		this.oneBasedLineNumber = oneBasedLineNumber;
		this.zeroBasedColumnNumber = zeroBasedColumnNumber;
	}
	
}
