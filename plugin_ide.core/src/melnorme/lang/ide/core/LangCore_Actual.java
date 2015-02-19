package melnorme.lang.ide.core;

import melnorme.lang.ide.core.operations.AbstractToolsManager;

public class LangCore_Actual {
	
	public static final String PLUGIN_ID = "com.github.rustdt.ide.core";
	public static final String NATURE_ID = PLUGIN_ID +".nature";
	
	public static final String BUILDER_ID = PLUGIN_ID + ".Builder";
	public static final String BUILD_PROBLEM_ID = PLUGIN_ID + ".marker_problem";
	
	public static AbstractToolsManager createToolManagerSingleton() {
		return new AbstractToolsManager() { };
	}
	
}