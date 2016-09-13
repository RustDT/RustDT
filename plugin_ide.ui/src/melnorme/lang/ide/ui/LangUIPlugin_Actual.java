package melnorme.lang.ide.ui;

import java.util.List;

import org.eclipse.ui.texteditor.ITextEditor;

import com.github.rustdt.ide.ui.RustImages;
import com.github.rustdt.ide.ui.editor.RustFmtEditorOperation;
import com.github.rustdt.ide.ui.text.RustAutoEditStrategy;

import melnorme.lang.ide.core.LangCore_Actual;
import melnorme.lang.ide.core.text.format.ILastKeyInfoProvider;
import melnorme.lang.ide.ui.editor.hover.ILangEditorTextHover;
import melnorme.lang.ide.ui.editor.text.LangAutoEditsPreferencesAccess;
import melnorme.lang.ide.ui.views.StructureElementLabelProvider;
import melnorme.lang.tooling.structure.StructureElement;
import melnorme.lang.tooling.structure.StructureElementKind;

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
	
	public static RustAutoEditStrategy createAutoEditStrategy(String contentType, 
		ILastKeyInfoProvider lastKeyInfoProvider) {
		return new RustAutoEditStrategy(contentType, new LangAutoEditsPreferencesAccess(), lastKeyInfoProvider);
	}
	
	public static StructureElementLabelProvider getStructureElementLabelProvider() {
		return new StructureElementLabelProvider() {
			@Override
			protected String getTypeDescriptionPrefix(StructureElement structureElement) {
				if(structureElement.getKind() == StructureElementKind.FUNCTION) {
					return " ";
				}
				return super.getTypeDescriptionPrefix(structureElement);
			}
		};
	}
	
	/* ----------------- UI messages:  ----------------- */
	
	public static final String BUILD_ConsoleName = LangCore_Actual.NAME_OF_LANGUAGE + " Build";
	public static final String ENGINE_TOOLS_ConsoleName = LangCore_Actual.NAME_OF_LANGUAGE + " Tools Log";
	
	
	/* -----------------  ----------------- */
	
	public static RustFmtEditorOperation getFormatOperation(ITextEditor editor) {
		return new RustFmtEditorOperation(editor);
	}
	
}