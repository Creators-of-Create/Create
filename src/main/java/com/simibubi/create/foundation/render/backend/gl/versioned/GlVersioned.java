package com.simibubi.create.foundation.render.backend.gl.versioned;

import org.lwjgl.opengl.GLCapabilities;

/**
 * This interface should be implemented by enums such that the
 * last defined variant <em>always</em> returns <code>true</code>.
 */
public interface GlVersioned {
	/**
	 * Queries whether this variant is supported by the current system.
	 *
	 * @param caps The {@link GLCapabilities} reported by the current system.
	 * @return <code>true</code> if this variant is supported, or if this is the last defined variant.
	 */
	boolean supported(GLCapabilities caps);
}
