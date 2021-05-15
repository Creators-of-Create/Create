package com.jozufozu.flywheel.backend.gl;

import com.jozufozu.flywheel.backend.Backend;

public class GlVertexArray extends GlObject {
	public GlVertexArray() {
		setHandle(Backend.compat.vao.genVertexArrays());
	}

	public void bind() {
		Backend.compat.vao.bindVertexArray(handle());
	}

	public void unbind() {
		Backend.compat.vao.bindVertexArray(0);
	}

	protected void deleteInternal(int handle) {
		Backend.compat.vao.deleteVertexArrays(handle);
	}
}
