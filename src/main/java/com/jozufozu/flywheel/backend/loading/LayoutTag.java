package com.jozufozu.flywheel.backend.loading;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jozufozu.flywheel.backend.gl.GlPrimitiveType;

public class LayoutTag {

	public static final Pattern pattern = Pattern.compile("Layout\\((\\w+)(?:\\s*,\\s*(\\w*))?\\)");

	final GlPrimitiveType type;
	final boolean normalized;

	public LayoutTag(Matcher matcher) {
		type = GlPrimitiveType.byName(matcher.group(1));
		normalized = Boolean.parseBoolean(matcher.group(2));
	}
}
