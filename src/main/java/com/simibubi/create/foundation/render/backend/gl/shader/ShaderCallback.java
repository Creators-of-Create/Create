package com.simibubi.create.foundation.render.backend.gl.shader;

/**
 * A Callback for when a shader is called. Used to define shader uniforms.
 */
@FunctionalInterface
public interface ShaderCallback<P extends GlProgram> {

	void call(P program);

	default ShaderCallback<P> andThen(ShaderCallback<P> other) {
		return program -> {
			call(program);
			other.call(program);
		};
	}
}
