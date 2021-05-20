package com.jozufozu.flywheel.backend;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.core.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.IMultiProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderSpecLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderTransformer;

public abstract class ShaderContext<P extends GlProgram> {

	public final Map<ProgramSpec, IMultiProgram<P>> programs = new HashMap<>();

	protected final ShaderSpecLoader<P> specLoader;
	protected ShaderTransformer transformer = new ShaderTransformer();

	public ShaderContext(ShaderSpecLoader<P> specLoader) {
		this.specLoader = specLoader;
	}

	// TODO: Untangle the loading functions

	/**
	 * Load all programs associated with this context. This might be just one, if the context is very specialized.
	 */
	public abstract void load(ShaderLoader loader);

	public void loadProgramFromSpec(ShaderLoader loader, ProgramSpec programSpec) {

		try {
			programs.put(programSpec, specLoader.create(loader, this, programSpec));

			Backend.log.debug("Loaded program {}", programSpec.name);
		} catch (Exception e) {
			Backend.log.error("program '{}': {}", programSpec.name, e.getMessage());
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

	public P getProgram(ProgramSpec spec) {
		return programs.get(spec).get();
	}

}
