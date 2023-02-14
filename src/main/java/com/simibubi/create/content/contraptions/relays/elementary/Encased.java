package com.simibubi.create.content.contraptions.relays.elementary;

import net.minecraft.world.level.block.Block;

/**
 * Implement this interface to indicate that this block is an EncasedBlock
 */
public interface Encased {

	Block getCasing();

	void setCasing(Block casing);
}
