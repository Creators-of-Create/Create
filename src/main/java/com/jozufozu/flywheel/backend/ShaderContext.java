package com.jozufozu.flywheel.backend;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderTransformer;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

import net.minecraft.util.ResourceLocation;

public abstract class ShaderContext<P extends GlProgram> {

	protected final Map<ResourceLocation, IMultiProgram<P>> programs = new HashMap<>();

	protected ShaderTransformer transformer = new ShaderTransformer();

	public ShaderContext() { }

	// TODO: Untangle the loading functions

	/**
	 * Load all programs associated with this context. This might be just one, if the context is very specialized.
	 */
	public abstract void load(ShaderLoader loader);

	protected abstract IMultiProgram<P> loadSpecInternal(ShaderLoader loader, ProgramSpec spec);

	public void loadProgramFromSpec(ShaderLoader loader, ProgramSpec programSpec) {

		try {
			programs.put(programSpec.name, loadSpecInternal(loader, programSpec));

			Backend.log.debug("Loaded program {}", programSpec.name);
		} catch (Exception e) {
			Backend.log.error("Program '{}': {}", programSpec.name, e);
			loader.notifyError();
		}
	}

	public Program loadProgram(ShaderLoader loader, ProgramSpec spec, Collection<String> defines) {
		Shader vertexFile = loader.source(spec.vert, ShaderType.VERTEX);
		Shader fragmentFile = loader.source(spec.frag, ShaderType.FRAGMENT);

		transformer.transformSource(vertexFile);
		transformer.transformSource(fragmentFile);

		if (defines != null) {
			vertexFile.defineAll(defines);
			fragmentFile.defineAll(defines);
		}

		Program program = loader.loadProgram(spec.name, vertexFile, fragmentFile);

		preLink(program);

		return program.link();
	}

	protected void preLink(Program program) {

	}

	public P getProgram(ResourceLocation spec) {
		return programs.get(spec).get();
	}

}
