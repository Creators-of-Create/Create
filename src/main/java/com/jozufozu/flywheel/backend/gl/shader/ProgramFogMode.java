package com.jozufozu.flywheel.backend.gl.shader;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlFog;

public abstract class ProgramFogMode {

	public abstract void bind();

	public static class None extends ProgramFogMode {

		public None(GlProgram program) {

		}

		@Override
		public void bind() {

		}
	}

	public static class Linear extends ProgramFogMode {
		private final int uFogColor;
		private final int uFogRange;

		public Linear(GlProgram program) {
			this.uFogColor = program.getUniformLocation("uFogColor");
			this.uFogRange = program.getUniformLocation("uFogRange");
		}

		@Override
		public void bind() {
			GL20.glUniform2f(uFogRange, GlFog.getFogStart(), GlFog.getFogEnd());
			GL20.glUniform4fv(uFogColor, GlFog.FOG_COLOR);
		}
	}

	public static class Exp2 extends ProgramFogMode {
		private final int uFogColor;
		private final int uFogDensity;

		public Exp2(GlProgram program) {
			this.uFogColor = program.getUniformLocation("uFogColor");
			this.uFogDensity = program.getUniformLocation("uFogDensity");
		}

		@Override
		public void bind() {
			GL20.glUniform1f(uFogDensity, GlFog.getFogDensity());
			GL20.glUniform4fv(uFogColor, GlFog.FOG_COLOR);
		}
	}

	public interface Factory {
		ProgramFogMode create(GlProgram program);
	}
}
