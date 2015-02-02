package com.github.rustdt.ide.ui.text;

import java.util.ArrayList;
import java.util.List;

import melnorme.lang.ide.ui.text.coloring.AbstractLangScanner;

import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * Sample LANGUAGE code scanner
 */
public class RustCodeScanner extends AbstractLangScanner {
	
	private static String tokenPrefProperties[] = new String[] {
		RustColorPreferences.DEFAULT.key,
		RustColorPreferences.KEYWORDS.key,
		RustColorPreferences.KEYWORDS_VALUES.key,
		RustColorPreferences.OPERATORS.key
	};
	
	public RustCodeScanner(ITokenStoreFactory factory) {
		super(factory.createTokenStore(tokenPrefProperties));
		setRules(createRules());
	}
	
	protected List<IRule> createRules() {
		List<IRule> rules = new ArrayList<IRule>();
		
		IToken tkOther = getToken(RustColorPreferences.DEFAULT.key);
		IToken tkKeywords = getToken(RustColorPreferences.KEYWORDS.key);
		IToken tkKeywordValues = getToken(RustColorPreferences.KEYWORDS_VALUES.key);
		IToken tkOperators = getToken(RustColorPreferences.OPERATORS.key);
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new LangWhitespaceDetector()));
		
		WordRule wordRule = new WordRule(new JavaWordDetector(), tkOther);
		
		wordRule.addWord("keyword",  tkKeywords);

		wordRule.addWord("null", tkKeywordValues);
		wordRule.addWord("true", tkKeywordValues);
		wordRule.addWord("false", tkKeywordValues);
		
		
		wordRule.addWord("==",  tkOperators);
		wordRule.addWord("!=",  tkOperators);
		
		rules.add(wordRule);
		
		setDefaultReturnToken(tkOther);
		return rules;
	}
	
}