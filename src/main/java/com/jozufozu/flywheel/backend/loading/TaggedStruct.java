package com.jozufozu.flywheel.backend.loading;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaggedStruct {

	public static final Pattern fieldPattern = Pattern.compile("(\\S+)\\s*(\\S+);");

	int srcStart, srcEnd;
	String source;
	String tag;
	String name;
	String body;

	Map<String, String> fields = new HashMap<>();

	public TaggedStruct(Matcher foundMatcher) {
		this.source = foundMatcher.group();

		srcStart = foundMatcher.start();
		srcEnd = foundMatcher.end();

		tag = foundMatcher.group(1);
		name = foundMatcher.group(2);
		body = foundMatcher.group(3);

		Matcher fielder = fieldPattern.matcher(body);

		while (fielder.find()) {
			fields.put(fielder.group(2), fielder.group(1));
		}
	}
}
