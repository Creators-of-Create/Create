package com.simibubi.create.foundation.utility;

import com.google.re2j.Pattern;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.data.Glob;

import java.util.regex.PatternSyntaxException;

public class LogisticParser {
	// Dynamic regex creation:
	// - If raw regex is enabled, uses a SAFE RE2/J parser
	// - Otherwise, uses the current glob parser
	public static Pattern dynamicToRegex(String pattern, String defaultPatternIfError) {
		boolean usingRawRegex = AllConfigs.server().logistics.useRegexForLogistics.get();
		if (usingRawRegex) {
			return toRegex(pattern, defaultPatternIfError);
		}
		return globToRegex(pattern, defaultPatternIfError);
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
		boolean usingRawRegex = AllConfigs.server().logistics.useRegexForLogistics.get();
		try {
			return Pattern.compile(Glob.toRegexPattern(pattern));
		} catch (PatternSyntaxException e) {
			return Pattern.compile(Glob.toRegexPattern(defaultGlobIfError));
		}
	}

	public static boolean anyMatches(Pattern pattern, String match) {
		return pattern.matcher(match).find();
	}
}
