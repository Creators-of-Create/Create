package com.jozufozu.flywheel.backend.loading;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public class Shader {
	// #flwinclude <"valid_namespace:valid/path_to_file.glsl">
	private static final Pattern includePattern = Pattern.compile("#flwinclude <\"([\\w\\d_]+:[\\w\\d_./]+)\">");

	public static final Pattern versionDetector = Pattern.compile("#version[^\\n]*");
	private static final Pattern decorator = Pattern.compile("#\\[([\\w_]*)]");

	public final ResourceLocation name;
	public ShaderType type;
	private String source;
	private final ShaderSources loader;

	private boolean parsed = false;
	final List<TaggedStruct> structs = new ArrayList<>(3);
	final Map<String, TaggedStruct> tag2Struct = new HashMap<>();
	final Map<String, TaggedStruct> name2Struct = new HashMap<>();

	public Shader(ShaderSources loader, ShaderType type, ResourceLocation name, String source) {
		this.loader = loader;
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

	public void processIncludes() {
		HashSet<ResourceLocation> seen = new HashSet<>();
		seen.add(name);

		source = includeRecursive(source, seen).collect(Collectors.joining("\n"));
	}

	private Stream<String> includeRecursive(String source, Set<ResourceLocation> seen) {
		return lines(source).flatMap(line -> {

			Matcher matcher = includePattern.matcher(line);

			if (matcher.find()) {
				String includeName = matcher.group(1);

				ResourceLocation include = new ResourceLocation(includeName);

				if (seen.add(include)) {
					try {
						return includeRecursive(loader.getShaderSource(include), seen);
					} catch (ShaderLoadingException e) {
						throw new ShaderLoadingException("could not resolve import: " + e.getMessage());
					}
				}

			}

			return Stream.of(line);
		});
	}

	public void printSource() {
		Backend.log.debug("Source for shader '" + name + "':");
		int i = 1;
		for (String s : source.split("\n")) {
			Backend.log.debug(String.format("%1$4s: ", i++) + s);
		}
	}

	public static Stream<String> lines(String s) {
		return new BufferedReader(new StringReader(s)).lines();
	}
}
