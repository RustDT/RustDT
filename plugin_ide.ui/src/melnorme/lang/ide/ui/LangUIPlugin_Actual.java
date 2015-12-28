package melnorme.lang.ide.ui;

import java.util.List;

import org.eclipse.jface.text.source.ISourceViewer;

import com.github.rustdt.ide.ui.RustImages;
import com.github.rustdt.ide.ui.text.RustAutoEditStrategy;

import melnorme.lang.ide.core.LangCore_Actual;
import melnorme.lang.ide.ui.editor.hover.ILangEditorTextHover;
import melnorme.lang.ide.ui.editor.text.LangAutoEditsPreferencesAccess;
import melnorme.lang.ide.ui.views.StructureElementLabelProvider;

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
	protected static void initTextHovers_afterProblemHover(
			List<Class<? extends ILangEditorTextHover<?>>> textHoverSpecifications) {
	}
	
	public static RustAutoEditStrategy createAutoEditStrategy(ISourceViewer sourceViewer, String contentType) {
		return new RustAutoEditStrategy(contentType, sourceViewer, new LangAutoEditsPreferencesAccess());
	}
	
	public static StructureElementLabelProvider getStructureElementLabelProvider() {
		return new StructureElementLabelProvider() {
		};
	}
	
	/* ----------------- UI messages:  ----------------- */
	
	public static final String TOOLS_CONSOLE_NAME = LangCore_Actual.LANGUAGE_NAME + " build";
	
	public static final String DAEMON_TOOL_Name = "Racer";
	public static final String DAEMON_TOOL_ConsoleName = "Rust tools log";
	
}