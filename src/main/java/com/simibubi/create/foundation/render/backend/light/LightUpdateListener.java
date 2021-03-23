package com.simibubi.create.foundation.render.backend.light;

import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;

/**
 * Anything can implement this, implementors should call {@link LightUpdater#startListening}
 * appropriately to make sure they get the updates they want.
 */
public interface LightUpdateListener {

	/**
	 * Called when a light updates in a chunk the implementor cares about.
	 */
	void onLightUpdate(ILightReader world, LightType type, GridAlignedBB changed);

	/**
	 * Called when the server sends light data to the client.
	 */
	default void onLightPacket(ILightReader world, int chunkX, int chunkZ) {
		GridAlignedBB changedVolume = GridAlignedBB.fromChunk(chunkX, chunkZ);

		onLightUpdate(world, LightType.BLOCK, changedVolume);
		onLightUpdate(world, LightType.SKY, changedVolume);
	}
}
