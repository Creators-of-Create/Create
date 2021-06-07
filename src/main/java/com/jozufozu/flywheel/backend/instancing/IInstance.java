package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;

import net.minecraft.util.math.BlockPos;

/**
 * A general interface providing information about any type of thing that could use Flywheel's instanced rendering.
 * Right now, that's only {@link TileInstanceManager}, but there could be an entity equivalent in the future.
 */
public interface IInstance {

	BlockPos getWorldPosition();
}
