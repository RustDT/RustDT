package melnorme.lang.ide.ui.dialogs;


import java.util.List;
import java.util.Optional;

import org.eclipse.swt.widgets.Shell;

import melnorme.lang.ide.ui.LangUIPlugin_Actual;
import melnorme.lang.ide.ui.views.EnhancedSelectionDialog;
import melnorme.lang.ide.ui.views.StructureElementLabelProvider.AdditionalInfo;
import melnorme.lang.tooling.structure.StructureElement;

public class PickTypeDialog {
	public static Optional<StructureElement> show(Shell parent, List<StructureElement> elements,
		boolean matchEmptyStrings) {
		EnhancedSelectionDialog dialog =
			new EnhancedSelectionDialog(parent,
				LangUIPlugin_Actual.getStructureElementLabelProvider(AdditionalInfo.LOCATION));
		
		dialog.setTitle("Open Type");
		dialog.setHelpAvailable(false);
		dialog.setMatchEmptyString(matchEmptyStrings);
		dialog.setMessage("Enter type name prefix or pattern (*, ? or camel case):");
		dialog.setElements(elements.toArray());
		dialog.open();
		
		return Optional.ofNullable((StructureElement) dialog.getFirstResult());
	}
}
