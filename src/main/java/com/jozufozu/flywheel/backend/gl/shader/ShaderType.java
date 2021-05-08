package com.jozufozu.flywheel.backend.gl.shader;

import org.lwjgl.opengl.GL20;

public enum ShaderType {
	VERTEX("vertex", GL20.GL_VERTEX_SHADER),
	FRAGMENT("fragment", GL20.GL_FRAGMENT_SHADER),
	;

	public final String name;
	public final int glEnum;

	ShaderType(String name, int glEnum) {
		this.name = name;
		this.glEnum = glEnum;
	}
}
