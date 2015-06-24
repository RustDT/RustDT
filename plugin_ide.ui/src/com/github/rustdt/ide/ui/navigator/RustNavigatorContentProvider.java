package com.github.rustdt.ide.ui.navigator;

import melnorme.lang.ide.ui.views.AbstractNavigatorContentProvider;

public class RustNavigatorContentProvider extends AbstractNavigatorContentProvider {
	
	@Override
	protected LangNavigatorSwitcher_HasChildren hasChildren_switcher() {
		return new LangNavigatorSwitcher_HasChildren() {
		};
	}
	
	@Override
	protected LangNavigatorSwitcher_GetChildren getChildren_switcher() {
		return new LangNavigatorSwitcher_GetChildren() {
		};
	}
	
	@Override
	protected LangNavigatorSwitcher_GetParent getParent_switcher() {
		return new LangNavigatorSwitcher_GetParent() {
		};
	}
	
}