package com.jozufozu.flywheel.backend.model;

public class ModelRenderer {

	protected BufferedModel model;

	public ModelRenderer(BufferedModel model) {
		this.model = model;
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public void draw() {
		if (!model.valid()) return;

		model.setupState();
		model.drawCall();
		model.clearState();
	}

	public void delete() {
		model.delete();
	}
}
