/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling.cargo;

import static melnorme.utilbox.core.CoreUtil.array;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import melnorme.utilbox.collections.HashSet2;
import melnorme.utilbox.status.Severity;
import melnorme.utilbox.status.StatusException;

public class RustNamingRules {
	
	public static String getCrateNameRef(String fileName) {
		if(fileName.endsWith(".rs")) {
			String crateName = fileName.substring(0, fileName.length()-3);
			if(crateName.isEmpty()) {
				return null;
			}
			
	        Pattern crateNamePattern = Pattern.compile("[\\w\\-]*");
	        if(crateNamePattern.matcher(crateName).matches()) {
	        	return crateName;
	        }
		}
		return null;
	}
	
	public static void validateCrateName(String crateName) throws StatusException {
		for(int ix = 0; ix < crateName.length(); ix++) {
			char ch = crateName.charAt(ix);
			if(Character.isAlphabetic(ch) || ch == '_' || ch == '-') {
				continue;
			}
			throw new StatusException(Severity.ERROR, 
				MessageFormat.format("Invalid character `{0}` in crate name `{1}`.", ch, crateName) );
		}
		
		if(CRATE_NAME_BLACKLIST.contains(crateName)) {
			throw new StatusException(Severity.ERROR, 
				MessageFormat.format("Invalid crate name `{0}`, blacklisted keyword.", crateName) );
		}
	}
	
	public static HashSet2<String> CRATE_NAME_BLACKLIST = new HashSet2<String>().addElements(array( 
			"abstract", "alignof", "as", "become", "box",
			"break", "const", "continue", "crate", "do", "dyn",
			"else", "enum", "extern", "false", "final",
			"fn", "for", "if", "impl", "in",
			"let", "loop", "macro", "match", "mod",
			"move", "mut", "offsetof", "override", "priv",
			"proc", "pub", "pure", "ref", "return",
			"self", "sizeof", "static", "struct",
			"super", "test", "trait", "true", "type", "typeof",
			"unsafe", "unsized", "use", "virtual", "where",
			"while", "yield" 
	));
}