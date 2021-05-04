package com.jozufozu.flywheel.backend.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.ResourceUtil;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.shader.FogSensitiveProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderSpecLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public class WorldContext<P extends BasicProgram> extends ShaderContext<P> {

	private static final Pattern builtinPattern = Pattern.compile("#flwbuiltins");

	public static final WorldContext<BasicProgram> INSTANCE = new WorldContext<>(new ResourceLocation("create", "std"), new FogSensitiveProgram.SpecLoader<>(BasicProgram::new));

	private final ShaderSpecLoader<P> loader;

	public final ResourceLocation frag;
	public final ResourceLocation vert;

	public WorldContext(ResourceLocation root, ShaderSpecLoader<P> loader) {
		super(root);
		this.frag = ResourceUtil.subPath(root, "/builtin.frag");
		this.vert = ResourceUtil.subPath(root, "/builtin.vert");

		this.loader = loader;
	}

	@Override
	public String preProcess(ShaderLoader loader, String shaderSrc, ShaderType type) {
		return ShaderLoader.lines(shaderSrc).flatMap(line -> {
			Matcher matcher = builtinPattern.matcher(line);

			if (matcher.find()) {
				ResourceLocation builtins;

				switch (type) {
					case FRAGMENT:
						builtins = frag;
						break;
					case VERTEX:
						builtins = vert;
						break;
					default:
						builtins = null;
				}

				String includeSource = loader.getShaderSource(builtins);

				return ShaderLoader.lines(includeSource);
			}

			return Stream.of(line);
		}).collect(Collectors.joining("\n"));
	}

	public ResourceLocation getFrag() {
		return frag;
	}

	public ResourceLocation getVert() {
		return vert;
	}

	@Override
	public ShaderSpecLoader<P> getLoader() {
		return loader;
	}
}
