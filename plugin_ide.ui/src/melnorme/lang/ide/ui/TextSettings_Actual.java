/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.ide.ui;

import melnorme.lang.ide.ui.editor.AbstractLangEditor;
import melnorme.lang.ide.ui.text.LangDocumentPartitionerSetup;
import melnorme.utilbox.core.fntypes.Function;
import melnorme.utilbox.misc.ArrayUtil;

import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import com.github.rustdt.ide.ui.editor.RustSimpleSourceViewerConfiguration;
import com.github.rustdt.ide.ui.editor.RustSourceViewerConfiguration;
import com.github.rustdt.ide.ui.text.RustDocumentSetupParticipant;
import com.github.rustdt.ide.ui.text.RustPartitionScanner;


public class TextSettings_Actual {
	
	public static final String PARTITIONING_ID = "com.github.rustdt.Partitioning";
	
	public static final String[] PARTITION_TYPES = LangPartitionTypes.PARTITION_TYPES;
	
	public static enum LangPartitionTypes {
		CODE, COMMENT, DOC_COMMENT, STRING, RAW_STRING, CHARACTER, ATTRIBUTE;
		
		public String getId() {
			if(ordinal() == 0) {
				return IDocument.DEFAULT_CONTENT_TYPE;
			}
			return toString();
		}
		
		public static final String[] PARTITION_TYPES = ArrayUtil.map(values(), 
			new Function<LangPartitionTypes, String>() {
				@Override
				public String evaluate(LangPartitionTypes obj) {
					return obj.getId();
				}
			}, String.class
		);
		
	}
	
	public static IPartitionTokenScanner createPartitionScanner() {
		return new RustPartitionScanner();
	}
	
	public static LangDocumentPartitionerSetup createDocumentSetupHelper() {
		return new RustDocumentSetupParticipant();
	}
	
	public static RustSourceViewerConfiguration createSourceViewerConfiguration(
			IPreferenceStore preferenceStore, AbstractLangEditor editor) {
		IColorManager colorManager = LangUIPlugin.getInstance().getColorManager();
		return new RustSourceViewerConfiguration(preferenceStore, colorManager, editor);
	}
	
	public static RustSimpleSourceViewerConfiguration createSimpleSourceViewerConfiguration(
			IPreferenceStore preferenceStore, IColorManager colorManager) {
		return new RustSimpleSourceViewerConfiguration(preferenceStore, colorManager);
	}
	
}