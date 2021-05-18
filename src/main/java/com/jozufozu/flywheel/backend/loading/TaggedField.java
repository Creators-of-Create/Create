package com.jozufozu.flywheel.backend.loading;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaggedField {
	public static final Pattern fieldPattern = Pattern.compile("(?:#\\[([^\\n]*)]\\s*)?(\\S+)\\s*(\\S+);");

	public String annotation;
	public String name;
	public String type;
	public LayoutTag layout;

	public TaggedField(Matcher fieldMatcher) {
		annotation = fieldMatcher.group(1);
		type = fieldMatcher.group(2);
		name = fieldMatcher.group(3);

		if (annotation != null) {
			Matcher matcher = LayoutTag.pattern.matcher(annotation);

			if (matcher.find()) {
				layout = new LayoutTag(matcher);
			}
		}
	}

	public String getAnnotation() {
		return annotation;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}


	@Override
	public String toString() {
		return "TaggedField{" +
				"name='" + name + '\'' +
				", type='" + type + '\'' +
				'}';
	}
}
