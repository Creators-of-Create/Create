package com.simibubi.create.foundation.utility;

import java.util.regex.PatternSyntaxException;

import com.google.re2j.Pattern;

import net.createmod.catnip.data.Glob;

public class LogisticParser {
	// Dynamic regex creation:
	// - If raw regex is enabled, uses a SAFE RE2/J parser
	// - Otherwise, uses the current glob parser
	public static Pattern dynamicToRegex(String pattern, String defaultPatternIfError, boolean usingRawRegex) {
		return usingRawRegex ? toRegex(pattern, defaultPatternIfError) : globToRegex(pattern, defaultPatternIfError);
		//	return AllConfigs.server().extras.enableAdvancedRegex.get()
		//		? toRegex(pattern, defaultPatternIfError)
		//		: globToRegex(pattern, defaultPatternIfError);
	}

	// Basic regex creation:
	// - Uses a SAFE RE2/J parser
	public static Pattern toRegex(String pattern, String defaultPatternIfError) {
		try {
			return Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			return Pattern.compile(defaultPatternIfError);
		}
	}

	// Glob regex creation:
	// - Uses the current glob parser
	public static Pattern globToRegex(String pattern, String defaultGlobIfError) {
		try {
			return Pattern.compile(Glob.toRegexPattern(pattern));
		} catch (PatternSyntaxException e) {
			return Pattern.compile(Glob.toRegexPattern(defaultGlobIfError));
		}
	}

	public static boolean anyMatches(Pattern pattern, String match) {
		return pattern.matcher(match).find();
	}

	public static boolean matchesAll(String pattern, boolean useRegex) {
		return pattern.equals(useRegex ? ".*" : "*");
	}
}
