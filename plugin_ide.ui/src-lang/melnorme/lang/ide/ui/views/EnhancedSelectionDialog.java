package melnorme.lang.ide.ui.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.FilteredList.FilterMatcher;
import org.eclipse.ui.dialogs.SearchPattern;

import melnorme.lang.tooling.structure.StructureElement;

/**
 * Enhanced search dialog with support for wildcards, camel case searches and the like
 */
public final class EnhancedSelectionDialog extends ElementListSelectionDialog {
	SearchPattern searchPatternWithCamelCase = new SearchPattern();
	
	public EnhancedSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}
	
	@Override
	protected FilteredList createFilteredList(Composite parent) {
		FilteredList filteredList = super.createFilteredList(parent);
		filteredList.setFilterMatcher(new FilterMatcher() {
			@Override
			public void setFilter(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
				searchPatternWithCamelCase.setPattern(pattern);
			}
			
			@Override
			public boolean match(Object element) {
				String elementText = ((StructureElement) element).getName();
				return searchPatternWithCamelCase.matches(elementText);
			}
		});
		return filteredList;
	}
}