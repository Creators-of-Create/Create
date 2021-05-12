package com.jozufozu.flywheel.backend.gl.shader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.loading.ProcessingStage;
import com.jozufozu.flywheel.backend.loading.Shader;

public class ShaderConstants implements ProcessingStage {
	public static final ShaderConstants EMPTY = new ShaderConstants();

	private final ArrayList<String> defines;

	public ShaderConstants() {
		defines = new ArrayList<>();
	}

	public ShaderConstants(ShaderConstants other) {
		this.defines = Lists.newArrayList(other.defines);
	}

	public static ShaderConstants define(String def) {
		return new ShaderConstants().def(def);
	}

	public ShaderConstants def(String def) {
		defines.add(def);
		return this;
	}

	public ShaderConstants defineAll(Collection<String> defines) {
		this.defines.addAll(defines);
		return this;
	}

	public ArrayList<String> getDefines() {
		return defines;
	}

	public Stream<String> directives() {
		return defines.stream().map(it -> "#define " + it);
	}

	@Override
	public void process(Shader shader) {
		shader.setSource(new BufferedReader(new StringReader(shader.getSource())).lines().flatMap(line -> {
			Stream<String> map = Stream.of(line);

			if (line.startsWith("#version")) {
				map = Stream.concat(map, directives());
			}

			return map;
		}).collect(Collectors.joining("\n")));
	}
}
