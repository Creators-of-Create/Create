package com.jozufozu.flywheel.backend.gl;

import com.jozufozu.flywheel.backend.Backend;

public class GlVertexArray extends GlObject {
	public GlVertexArray() {
		setHandle(Backend.getInstance().compat.vao.genVertexArrays());
	}

	public void bind() {
		Backend.getInstance().compat.vao.bindVertexArray(handle());
	}

	public void unbind() {
		Backend.getInstance().compat.vao.bindVertexArray(0);
	}

	protected void deleteInternal(int handle) {
		Backend.getInstance().compat.vao.deleteVertexArrays(handle);
	}
}
