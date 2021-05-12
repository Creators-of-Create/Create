package com.jozufozu.flywheel.backend.loading;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.ResourceLocation;

public class ParsedShader {
	private static final Pattern decorator = Pattern.compile("#\\[([\\w_]*)]");
	private static final Pattern taggedStruct = Pattern.compile("#\\[([\\w_]*)]\\s*struct\\s+([\\w\\d_]*)\\s*\\{(\\s*(?:.*;\\s*\\n)+\\s*)}\\s*;");

	final ResourceLocation loc;
	final String src;

	final Map<String, TaggedStruct> tag2Struct = new HashMap<>();
	final Map<String, TaggedStruct> name2Struct = new HashMap<>();

	public ParsedShader(ResourceLocation loc, String src) {
		this.loc = loc;
		Matcher structs = taggedStruct.matcher(src);

		StringBuffer strippedSrc = new StringBuffer();
		while (structs.find()) {
			TaggedStruct struct = new TaggedStruct(structs);

			structs.appendReplacement(strippedSrc, decorator.matcher(struct.source).replaceFirst(""));

			tag2Struct.put(struct.tag, struct);
			name2Struct.put(struct.name, struct);
		}
		structs.appendTail(strippedSrc);

		this.src = strippedSrc.toString();
	}

	public TaggedStruct getTag(String tag) {
		return tag2Struct.get(tag);
	}

	public TaggedStruct getStruct(String name) {
		return name2Struct.get(name);
	}
}
