package melnorme.lang.ide.ui;

import java.util.List;

import org.eclipse.jface.text.source.ISourceViewer;

import melnorme.lang.ide.ui.editor.ILangEditorTextHover;

import com.github.rustdt.ide.ui.RustImages;
import com.github.rustdt.ide.ui.text.RustAutoEditStrategy;

/**
 * Actual/concrete IDE constants and other bindings, for Lang UI code. 
 */
public final class LangUIPlugin_Actual {
	
	public static final String PLUGIN_ID = "com.github.rustdt.ide.ui";
	
	public static final String ROOT_PREF_PAGE_ID = PLUGIN_ID + ".PreferencePages.Root";
	
	public static final String RULER_CONTEXT = "#RustRulerContext";
	public static final String EDITOR_CONTEXT = "#RustEditorContext";
	
	// ID to start the debug plugin automatically, if present
	protected static final String DEBUG_PLUGIN_ID = "com.github.rustdt.ide.debug";
	
	protected static final Class<?> PLUGIN_IMAGES_CLASS = RustImages.class;
	
	@SuppressWarnings("unused")
	protected static void initTextHovers( List<Class<? extends ILangEditorTextHover<?>>> textHoverSpecifications) {
	}
	
	public static RustAutoEditStrategy createAutoEditStrategy(ISourceViewer sourceViewer, String contentType) {
		return new RustAutoEditStrategy(contentType, sourceViewer);
	}
	
	/* ----------------- UI messages:  ----------------- */
	
	public static final String LANGUAGE_NAME = "Rust";
	public static final String DAEMON_TOOL_Name = "Racer";
	public static final String DAEMON_TOOL_ConsoleName = "RustDT Racer log";
	
}