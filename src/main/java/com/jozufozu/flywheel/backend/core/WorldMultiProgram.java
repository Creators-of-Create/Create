package com.jozufozu.flywheel.backend.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
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

public class WorldMultiProgram<P extends GlProgram> implements IMultiProgram<P> {

	private final Map<GlFogMode, P> programs;
	private final List<P> debugPrograms;

	public WorldMultiProgram(Map<GlFogMode, P> programs, List<P> debugPrograms) {
		this.programs = programs;
		this.debugPrograms = debugPrograms;
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
			List<P> debugModes = new ArrayList<>(2);

			String[] modes = new String[]{"NORMAL_DEBUG", "RAINBOW_DEBUG"};

			for (String mode : modes) {
				Program builder = ctx.loadProgram(loader, spec, Collections.singletonList(mode));

				debugModes.add(factory.create(builder, null));
			}

			Map<GlFogMode, P> programs = new EnumMap<>(GlFogMode.class);

			for (GlFogMode fogMode : GlFogMode.values()) {
				Program builder = ctx.loadProgram(loader, spec, fogMode.getDefines());

				programs.put(fogMode, factory.create(builder, Collections.singletonList(fogMode)));
			}

			return new WorldMultiProgram<>(programs, debugModes);
		}

	}

}
