package melnorme.lang.ide.ui;

import java.util.List;

import org.eclipse.jface.text.source.ISourceViewer;

import melnorme.lang.ide.ui.editor.ILangEditorTextHover;
import com.github.rustdt.ide.ui.LANGUAGE_Images;
import com.github.rustdt.ide.ui.editor.LANGUAGE_AutoEditStrategy;

/**
 * Actual/concrete IDE constants and other bindings, for Lang UI code. 
 */
public final class LangUIPlugin_Actual {
	
	public static final String PLUGIN_ID = "com.github.rustdt.ide.ui";
	
	public static final String RULER_CONTEXT = "#LANGUAGE_RulerContext";
	public static final String EDITOR_CONTEXT = "#LANGUAGE_EditorContext";
	
	// ID to start the debug plugin automatically, if present
	protected static final String DEBUG_PLUGIN_ID = "com.github.rustdt.ide.debug";
	
	protected static final Class<?> PLUGIN_IMAGES_CLASS = LANGUAGE_Images.class;
	
	@SuppressWarnings("unused")
	protected static void initTextHovers( List<Class<? extends ILangEditorTextHover<?>>> textHoverSpecifications) {
	}
	
	public static LANGUAGE_AutoEditStrategy createAutoEditStrategy(ISourceViewer sourceViewer, String contentType) {
		return new LANGUAGE_AutoEditStrategy(contentType, sourceViewer);
	}
	
	public static final String DAEMON_TOOL_ConsoleName = "lang_daemon log";
	
}