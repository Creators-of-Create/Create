package com.jozufozu.flywheel.backend.gl.shader;

import java.util.EnumMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.GlFog;
import com.jozufozu.flywheel.backend.gl.GlFogMode;

import net.minecraft.util.ResourceLocation;

public class FogSensitiveProgram<P extends GlProgram> implements IMultiProgram<P> {

	private final Map<GlFogMode, P> programs;

	public FogSensitiveProgram(Map<GlFogMode, P> programs) {
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

		private final FogProgramLoader<P> fogProgramLoader;

		public SpecLoader(FogProgramLoader<P> fogProgramLoader) {
			this.fogProgramLoader = fogProgramLoader;
		}

		@Override
		public IMultiProgram<P> create(ShaderLoader loader, ShaderContext<P> ctx, ProgramSpec spec) {
			Map<GlFogMode, P> programs = new EnumMap<>(GlFogMode.class);

			for (GlFogMode fogMode : GlFogMode.values()) {
				ShaderConstants defines = new ShaderConstants(spec.defines);

				defines.defineAll(fogMode.getDefines());

				GlProgram.Builder builder = spec.loadProgram(ctx, defines, loader);

				programs.put(fogMode, fogProgramLoader.create(builder.name, builder.program, fogMode.getFogFactory()));
			}

			return new FogSensitiveProgram<>(programs);
		}

	}

	public interface FogProgramLoader<P extends GlProgram> {

		P create(ResourceLocation name, int handle, ProgramFogMode.Factory fogFactory);
	}
}
