package melnorme.lang.ide.ui;

import org.eclipse.cdt.internal.ui.text.util.CColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import com.github.rustdt.ide.ui.editor.RustSimpleSourceViewerConfiguration;
import com.github.rustdt.ide.ui.text.RustPartitionScanner;


public class TextSettings_Actual {
	
	public static final String PARTITIONING_ID = "com.github.rustdt.Partitioning";
	
	public static final String[] PARTITION_TYPES = new String[] { 
		LangPartitionTypes.CODE,
		LangPartitionTypes.COMMENT,
		LangPartitionTypes.STRING
	};
	
	public static interface LangPartitionTypes {
		String CODE = IDocument.DEFAULT_CONTENT_TYPE;
		String COMMENT = "comment";
		String STRING = "string";
	}
	
	public static IPartitionTokenScanner createPartitionScanner() {
		return new RustPartitionScanner();
	}
	
	public static RustSimpleSourceViewerConfiguration createSimpleSourceViewerConfiguration(
			IPreferenceStore preferenceStore, CColorManager colorManager) {
		return new RustSimpleSourceViewerConfiguration(preferenceStore, colorManager);
	}
	
}