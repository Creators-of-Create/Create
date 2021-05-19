package com.jozufozu.flywheel.backend.gl.shader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.GlFog;
import com.jozufozu.flywheel.backend.gl.GlFogMode;
import com.jozufozu.flywheel.backend.loading.Program;

public class FogSensitiveProgram<P extends GlProgram> implements IMultiProgram<P> {

	private final Map<GlFogMode, P> programs;
	private final List<P> debugPrograms;

	public FogSensitiveProgram(Map<GlFogMode, P> programs, List<P> debugPrograms) {
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

		private final FogProgramLoader<P> fogProgramLoader;

		public SpecLoader(FogProgramLoader<P> fogProgramLoader) {
			this.fogProgramLoader = fogProgramLoader;
		}

		@Override
		public IMultiProgram<P> create(ShaderLoader loader, ShaderContext<P> ctx, ProgramSpec spec) {
			List<P> debugModes = new ArrayList<>(2);

			String[] modes = new String[]{"NORMAL_DEBUG", "RAINBOW_DEBUG"};

			for (String mode : modes) {
				Program builder = ctx.loadProgram(loader, spec, Collections.singletonList(mode));

				debugModes.add(fogProgramLoader.create(builder, GlFogMode.NONE.getFogFactory()));
			}

			Map<GlFogMode, P> programs = new EnumMap<>(GlFogMode.class);

			for (GlFogMode fogMode : GlFogMode.values()) {
				Program builder = ctx.loadProgram(loader, spec, fogMode.getDefines());

				programs.put(fogMode, fogProgramLoader.create(builder, fogMode.getFogFactory()));
			}

			return new FogSensitiveProgram<>(programs, debugModes);
		}

	}

	public interface FogProgramLoader<P extends GlProgram> {

		P create(Program program, ProgramFogMode.Factory fogFactory);
	}
}
