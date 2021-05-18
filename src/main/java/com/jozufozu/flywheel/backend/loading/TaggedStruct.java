package com.jozufozu.flywheel.backend.loading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaggedStruct {

	// https://regexr.com/5t207
	static final Pattern taggedStruct = Pattern.compile("#\\[(\\w*)]\\s*struct\\s+([\\w\\d]*)\\s*\\{([\\w\\d \\t#\\[\\](),;\\n]*)}\\s*;");

	int srcStart, srcEnd;
	String source;
	String tag;
	String name;
	String body;

	List<TaggedField> fields = new ArrayList<>(4);
	Map<String, String> fields2Types = new HashMap<>();

	public TaggedStruct(Matcher foundMatcher) {
		this.source = foundMatcher.group();

		srcStart = foundMatcher.start();
		srcEnd = foundMatcher.end();

		tag = foundMatcher.group(1);
		name = foundMatcher.group(2);
		body = foundMatcher.group(3);

		Matcher fielder = TaggedField.fieldPattern.matcher(body);

		while (fielder.find()) {
			fields.add(new TaggedField(fielder));
			fields2Types.put(fielder.group(2), fielder.group(1));
		}
	}

	public void addPrefixedAttributes(Program builder, String prefix) {
		for (TaggedField field : fields) {
			int attributeCount = TypeHelper.getAttributeCount(field.type);

			builder.addAttribute(prefix + field.name, attributeCount);
		}
	}
}
