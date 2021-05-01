package com.jozufozu.flywheel.backend.gl;

import org.lwjgl.opengl.GL20;

public class GlTexture extends GlObject {
	private final int textureType;

	public GlTexture(int textureType) {
		this.textureType = textureType;
		setHandle(GL20.glGenTextures());
	}

	@Override
	protected void deleteInternal(int handle) {
		GL20.glDeleteTextures(handle);
	}

	public void bind() {
		GL20.glBindTexture(textureType, handle());
	}

	public void unbind() {
		GL20.glBindTexture(textureType, 0);
	}
}
