package com.jozufozu.flywheel.backend.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.core.shader.ExtensibleGlProgram;
import com.jozufozu.flywheel.backend.core.shader.GlFog;
import com.jozufozu.flywheel.backend.core.shader.GlFogMode;
import com.jozufozu.flywheel.backend.core.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.IMultiProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderSpecLoader;
import com.jozufozu.flywheel.backend.loading.Program;

public class FogMultiProgram<P extends GlProgram> implements IMultiProgram<P> {

	private final Map<GlFogMode, P> programs;

	public FogMultiProgram(Map<GlFogMode, P> programs) {
		this.programs = programs;
	}

	@Override
	public P get() {
		return programs.get(GlFog.getFogMode());
	}

	@Override
	public void delete() {
		programs.values().forEach(GlProgram::delete);
	}

	public static class SpecLoader<P extends GlProgram> implements ShaderSpecLoader<P> {

		private final ExtensibleGlProgram.Factory<P> factory;

		public SpecLoader(ExtensibleGlProgram.Factory<P> factory) {
			this.factory = factory;
		}

		@Override
		public IMultiProgram<P> create(ShaderLoader loader, ShaderContext<P> ctx, ProgramSpec spec) {
			Map<GlFogMode, P> programs = new EnumMap<>(GlFogMode.class);

			for (GlFogMode fogMode : GlFogMode.values()) {
				Program builder = ctx.loadProgram(loader, spec, fogMode.getDefines());

				programs.put(fogMode, factory.create(builder, Collections.singletonList(fogMode)));
			}

			return new FogMultiProgram<>(programs);
		}

	}

}
