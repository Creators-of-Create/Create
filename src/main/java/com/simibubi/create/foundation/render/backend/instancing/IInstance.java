package com.simibubi.create.foundation.render.backend.instancing;

import net.minecraft.util.math.BlockPos;

/**
 * A general interface providing information about any type of thing that could use
 * Flywheel's instanced rendering. Right now, that's only {@link InstancedTileRenderer},
 * but there could be an entity equivalent in the future.
 */
public interface IInstance {

	BlockPos getWorldPosition();
}
