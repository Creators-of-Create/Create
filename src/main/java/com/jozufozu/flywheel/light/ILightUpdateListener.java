package com.jozufozu.flywheel.light;

import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;

/**
 * Anything can implement this, implementors should call {@link LightUpdater#startListening}
 * appropriately to make sure they get the updates they want.
 */
public interface ILightUpdateListener {

	/**
	 * Called when a light updates in a chunk the implementor cares about.
	 *
	 * @return true if this object is no longer valid and should not receive any more updates.
	 */
	boolean onLightUpdate(IBlockDisplayReader world, LightType type, GridAlignedBB changed);

	/**
	 * Called when the server sends light data to the client.
	 *
	 * @return true if this object is no longer valid and should not receive any more updates.
	 */
	default boolean onLightPacket(IBlockDisplayReader world, int chunkX, int chunkZ) {
		GridAlignedBB changedVolume = GridAlignedBB.from(chunkX, chunkZ);

		if (onLightUpdate(world, LightType.BLOCK, changedVolume))
			return true;

		return onLightUpdate(world, LightType.SKY, changedVolume);
	}
}
