package com.jozufozu.flywheel.backend.instancing;

import net.minecraft.world.World;

/**
 * Something (a TileEntity or Entity) that can be rendered using the instancing API.
 */
public interface IInstanceRendered {

	/**
	 * @return true if there are parts of the renderer that cannot be implemented with Flywheel.
	 */
	default boolean shouldRenderNormally() {
		return false;
	}

	World getWorld();
}
