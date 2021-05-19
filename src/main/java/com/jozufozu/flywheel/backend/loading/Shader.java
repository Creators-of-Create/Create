package com.jozufozu.flywheel.backend.loading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public class Shader {
	public static final Pattern versionDetector = Pattern.compile("#version[^\\n]*");
	private static final Pattern decorator = Pattern.compile("#\\[([\\w_]*)]");

	public final ResourceLocation name;
	public ShaderType type;
	private String source;

	private boolean parsed = false;
	final List<TaggedStruct> structs = new ArrayList<>(3);
	final Map<String, TaggedStruct> tag2Struct = new HashMap<>();
	final Map<String, TaggedStruct> name2Struct = new HashMap<>();

	public Shader(ShaderType type, ResourceLocation name, String source) {
		this.type = type;
		this.name = name;
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public TaggedStruct getTag(String tag) {
		checkAndParse();
		return tag2Struct.get(tag);
	}

	public TaggedStruct getStruct(String name) {
		checkAndParse();
		return name2Struct.get(name);
	}

	private void checkAndParse() {
		if (!parsed) {
			parsed = true;
			parseStructs();
		}
	}

	public void defineAll(Collection<String> defines) {
		Matcher matcher = versionDetector.matcher(source);

		if (matcher.find()) {
			StringBuffer sourceWithDefines = new StringBuffer();
			String lines = defines.stream().map(it -> "#define " + it).collect(Collectors.joining("\n"));

			matcher.appendReplacement(sourceWithDefines, matcher.group() + '\n' + lines);

			matcher.appendTail(sourceWithDefines);

			source = sourceWithDefines.toString();
		}
	}

	public void parseStructs() {
		Matcher structMatcher = TaggedStruct.taggedStruct.matcher(source);

		StringBuffer strippedSrc = new StringBuffer();

		while (structMatcher.find()) {
			TaggedStruct struct = new TaggedStruct(structMatcher);

			structs.add(struct);

			structMatcher.appendReplacement(strippedSrc, decorator.matcher(struct.source).replaceFirst(""));

			tag2Struct.put(struct.tag, struct);
			name2Struct.put(struct.name, struct);
		}
		structMatcher.appendTail(strippedSrc);

		this.source = strippedSrc.toString();
	}
}
