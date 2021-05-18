package com.jozufozu.flywheel.backend;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.IMultiProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.ShaderConstants;
import com.jozufozu.flywheel.backend.gl.shader.ShaderSpecLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderTransformer;

import net.minecraft.util.ResourceLocation;

public abstract class ShaderContext<P extends GlProgram> {

	public final Map<ProgramSpec, IMultiProgram<P>> programs = new HashMap<>();

	public final ResourceLocation root;
	protected final ShaderSpecLoader<P> specLoader;
	protected ShaderTransformer transformer = new ShaderTransformer();

	public ShaderContext(ResourceLocation root, ShaderSpecLoader<P> specLoader) {
		this.root = root;
		this.specLoader = specLoader;
	}

	// TODO: Untangle the loading functions

	/**
	 * Load all programs associated with this context. This might be just one, if the context is very specialized.
	 */
	public abstract void load(ShaderLoader loader);

	public void loadProgramFromSpec(ShaderLoader loader, ProgramSpec programSpec) {

		programs.put(programSpec, specLoader.create(loader, this, programSpec));

		Backend.log.debug("Loaded program {}", programSpec.name);
	}

	public Program loadProgram(ProgramSpec spec, ShaderConstants defines, ShaderLoader loader) {
		if (defines != null)
			transformer.pushStage(defines);

		Shader vertexFile = loader.source(spec.vert, ShaderType.VERTEX);
		Shader fragmentFile = loader.source(spec.frag, ShaderType.FRAGMENT);

		transformer.transformSource(vertexFile);
		transformer.transformSource(fragmentFile);

		Program program = loader.loadProgram(spec.name, vertexFile, fragmentFile);
		if (defines != null)
			transformer.popStage();

		preLink(program);

		return program.link();
	}

	protected void preLink(Program program) {

	}

	public P getProgram(ProgramSpec spec) {
		return programs.get(spec).get();
	}

	public ResourceLocation getRoot() {
		return root;
	}

}
