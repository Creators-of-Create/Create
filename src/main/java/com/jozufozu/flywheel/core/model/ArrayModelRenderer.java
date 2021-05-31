package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;

public class ArrayModelRenderer extends ModelRenderer {

	protected GlVertexArray vao;

	public ArrayModelRenderer(BufferedModel model) {
		super(model);
		vao = new GlVertexArray();

		vao.bind();
		model.setupState();
		vao.unbind();
		model.clearState();
	}

	public void draw() {
		if (!model.valid()) return;

		vao.bind();

		model.drawCall();

		vao.unbind();
	}
}
