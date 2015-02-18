package com.github.rustdt.ide.core;

import melnorme.lang.ide.core.LangCore_Actual;
import melnorme.lang.ide.core.LangNature;

public class RustNature extends LangNature {
	
	@Override
	protected String getBuilderId() {
		return LangCore_Actual.BUILDER_ID;
	}
	
}
