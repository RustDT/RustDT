package melnorme.lang.ide.core;

import melnorme.lang.ide.core.text.LangDocumentPartitionerSetup;
import java.util.function.Function;
import melnorme.utilbox.misc.ArrayUtil;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import com.github.rustdt.ide.core.text.RustDocumentSetupParticipant;
import com.github.rustdt.ide.core.text.RustPartitionScanner;


public class TextSettings_Actual {
	
	public static final String PARTITIONING_ID = "com.github.rustdt.Partitioning";
	
	public static enum LangPartitionTypes {
		CODE, 
		LINE_COMMENT, BLOCK_COMMENT, 
		DOC_LINE_COMMENT, DOC_BLOCK_COMMENT, 
		STRING, RAW_STRING, CHARACTER, LIFETIME, ATTRIBUTE;
		
		public String getId() {
			if(ordinal() == 0) {
				return IDocument.DEFAULT_CONTENT_TYPE;
			}
			return toString();
		}
		
	}
	
	public static IPartitionTokenScanner createPartitionScanner() {
		return new RustPartitionScanner();
	}
	
	public static LangDocumentPartitionerSetup createDocumentSetupHelper() {
		return new RustDocumentSetupParticipant();
	}
	
	/* ----------------- Common code ----------------- */
	
	public static final String[] PARTITION_TYPES = ArrayUtil.map(LangPartitionTypes.values(), 
		new Function<LangPartitionTypes, String>() {
			@Override
			public String apply(LangPartitionTypes obj) {
				return obj.getId();
			}
		}, String.class
	);
	
}