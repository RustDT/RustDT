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

import java.util.regex.Pattern;

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
	
}