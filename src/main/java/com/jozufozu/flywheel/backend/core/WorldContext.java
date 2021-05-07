package com.jozufozu.flywheel.backend.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ResourceUtil;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.shader.FogSensitiveProgram;
import com.jozufozu.flywheel.backend.gl.shader.IMultiProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderSpecLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;

import net.minecraft.util.ResourceLocation;

public class WorldContext<P extends BasicProgram> extends ShaderContext<P> {

	private static final Pattern builtinPattern = Pattern.compile("#flwbuiltins");

	public static final WorldContext<BasicProgram> INSTANCE = new WorldContext<>(new ResourceLocation("create", "std"), new FogSensitiveProgram.SpecLoader<>(BasicProgram::new));
	public static final WorldContext<CrumblingProgram> CRUMBLING = new WorldContext<>(new ResourceLocation("create", "crumbling"), new FogSensitiveProgram.SpecLoader<>(CrumblingProgram::new));

	private final ShaderSpecLoader<P> loader;

	final Map<ShaderType, ResourceLocation> builtins;

	public WorldContext(ResourceLocation root, ShaderSpecLoader<P> loader) {
		super(root);
		builtins = new EnumMap<>(ShaderType.class);
		builtins.put(ShaderType.FRAGMENT, ResourceUtil.subPath(root, "/builtin.frag"));
		builtins.put(ShaderType.VERTEX, ResourceUtil.subPath(root, "/builtin.vert"));

		this.loader = loader;
	}

	@Override
	public void load(ShaderLoader loader) {
		programs.values().forEach(IMultiProgram::delete);
		programs.clear();

		Backend.allMaterials()
				.stream()
				.map(MaterialSpec::getProgramSpec)
				.forEach(spec -> loadProgramFromSpec(loader, spec));
	}

	@Override
	public String preProcess(ShaderLoader loader, String shaderSrc, ShaderType type) {
		String builtinSrc = loader.getShaderSource(builtins.get(type));

		return builtinPattern.matcher(shaderSrc)
				.replaceFirst(builtinSrc);
	}

	@Override
	public ShaderSpecLoader<P> getLoader() {
		return loader;
	}
}
