package com.jozufozu.flywheel.core.shader;

import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

/**
 * Encapsulates any number of shader programs for use in similar contexts.
 * Allows the implementor to choose which shader program to use based on arbitrary state.
 *
 * @param <P>
 */
public interface IMultiProgram<P extends GlProgram> extends Supplier<P> {

	/**
	 * Get the shader program most suited for the current game state.
	 *
	 * @return The one true program.
	 */
	P get();

	/**
	 * Delete all shader programs encapsulated by your implementation.
	 */
	void delete();
}
