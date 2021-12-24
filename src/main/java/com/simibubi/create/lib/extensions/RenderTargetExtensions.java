package com.simibubi.create.lib.extensions;

public interface RenderTargetExtensions {
	/**
	 * Attempts to enable 8 bits of stencil buffer on this FrameBuffer.
	 * Modders must call this directly to set things up.
	 * This is to prevent the default cause where graphics cards do not support stencil bits.
	 * <b>Make sure to call this on the main render thread!</b>
	 */
	default void create$enableStencil() {}

	/**
	 * Returns wither or not this FBO has been successfully initialized with stencil bits.
	 * If not, and a modder wishes it to be, they must call enableStencil.
	 */
	default boolean create$isStencilEnabled() {
		return false;
	}
}
