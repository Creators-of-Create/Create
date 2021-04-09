package com.simibubi.create.foundation.render.backend.gl.shader;

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
}
