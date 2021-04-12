package com.simibubi.create.foundation.render.backend.gl.shader;

import com.simibubi.create.foundation.render.backend.ShaderLoader;

import net.minecraft.util.ResourceLocation;

public class SingleProgram<P extends GlProgram> implements IMultiProgram<P> {
	final P program;

	public SingleProgram(P program) {
		this.program = program;
	}

	@Override
	public P get() {
		return program;
	}

	@Override
	public void delete() {
		program.delete();
	}

	public static class SpecLoader<P extends GlProgram> implements ShaderSpecLoader<P> {
		final ProgramFactory<P> factory;

		public SpecLoader(ProgramFactory<P> factory) {
			this.factory = factory;
		}

		@Override
		public IMultiProgram<P> create(ShaderLoader loader, ProgramSpec<P> spec) {
			GlProgram.Builder builder = loader.loadProgram(spec);

			return new SingleProgram<>(factory.create(builder.name, builder.program));
		}
	}

	@FunctionalInterface
	public interface ProgramFactory<P extends GlProgram> {
		P create(ResourceLocation name, int handle);
	}
}
