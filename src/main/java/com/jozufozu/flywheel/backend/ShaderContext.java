package com.jozufozu.flywheel.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.core.shader.IMultiProgram;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.core.shader.spec.ProgramState;

import net.minecraft.util.ResourceLocation;

public abstract class ShaderContext<P extends GlProgram> {

	protected final Map<ResourceLocation, IMultiProgram<P>> programs = new HashMap<>();

	protected ShaderSources sourceRepo;

	/**
	 * Load all programs associated with this context. This might be just one, if the context is very specialized.
	 */
	public final void load(ShaderSources loader) {
		this.sourceRepo = loader;
		load();
	}

	protected abstract void load();

	public Program loadAndLink(ProgramSpec spec, @Nullable ProgramState state) {
		Shader vertexFile = getSource(ShaderType.VERTEX, spec.vert);
		Shader fragmentFile = getSource(ShaderType.FRAGMENT, spec.frag);

		if (state != null) {
			vertexFile.defineAll(state.getDefines());
			fragmentFile.defineAll(state.getDefines());
		}

		return link(loadProgram(spec.name, vertexFile, fragmentFile));
	}

	protected Shader getSource(ShaderType type, ResourceLocation name) {
		return sourceRepo.source(name, type);
	}

	protected Program link(Program program) {
		return program.link();
	}

	public P getProgram(ResourceLocation spec) {
		return programs.get(spec).get();
	}

	protected Program loadProgram(ResourceLocation name, Shader... shaders) {
		return loadProgram(name, Lists.newArrayList(shaders));
	}

	/**
	 * Ingests the given shaders, compiling them and linking them together after applying the transformer to the source.
	 *
	 * @param name    What should we call this program if something goes wrong?
	 * @param shaders What are the different shader stages that should be linked together?
	 * @return A program with all provided shaders attached
	 */
	protected Program loadProgram(ResourceLocation name, Collection<Shader> shaders) {
		List<GlShader> compiled = new ArrayList<>(shaders.size());
		try {
			Program builder = new Program(name);

			for (Shader shader : shaders) {
				GlShader sh = new GlShader(shader);
				compiled.add(sh);

				builder.attachShader(shader, sh);
			}

			return builder;
		} finally {
			compiled.forEach(GlObject::delete);
		}
	}

}
