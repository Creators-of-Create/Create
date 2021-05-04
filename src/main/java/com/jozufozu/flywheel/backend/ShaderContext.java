package com.jozufozu.flywheel.backend;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.IMultiProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.ShaderSpecLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public abstract class ShaderContext<P extends GlProgram> {

	public final Map<ProgramSpec, IMultiProgram<P>> programs = new HashMap<>();

	public final ResourceLocation root;

	public ShaderContext(ResourceLocation root) {
		this.root = root;
	}

	public abstract ShaderSpecLoader<P> getLoader();

	public abstract void load(ShaderLoader loader);

	public void loadProgramFromSpec(ShaderLoader loader, ProgramSpec programSpec) {

		programs.put(programSpec, getLoader().create(loader, this, programSpec));

		Backend.log.debug("Loaded program {}", programSpec.name);
	}

	public String preProcess(ShaderLoader loader, String shaderSrc, ShaderType type) {
		return shaderSrc;
	}

	public P getProgram(ProgramSpec spec) {
		return programs.get(spec).get();
	}

	public ResourceLocation getRoot() {
		return root;
	}

}
