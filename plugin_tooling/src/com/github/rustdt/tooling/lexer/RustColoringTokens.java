/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling.lexer;

public enum RustColoringTokens {
	WS,
	KEYWORD,
	KEYWORD_BOOL,
	KEYWORD_SELF,
	WORD,
	MACRO_CALL,
	MACRO_RULES,
	NUMBER,
	TRY_OP,
}