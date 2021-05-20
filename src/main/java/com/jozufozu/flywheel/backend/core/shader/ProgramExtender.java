package com.jozufozu.flywheel.backend.core.shader;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

/**
 * A factory interface for creating {@link IProgramExtension}s. These are what end up being passed in
 * during shader program construction.
 */
public interface ProgramExtender {

	/**
	 * Construct the extension, binding any necessary information using the provided {@link GlProgram}.
	 *
	 * @param program The program being extended.
	 * @return An extension object, possibly initialized using the program.
	 */
	IProgramExtension create(GlProgram program);
}
