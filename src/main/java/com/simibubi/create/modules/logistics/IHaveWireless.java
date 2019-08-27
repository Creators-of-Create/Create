package com.simibubi.create.modules.logistics;

import com.simibubi.create.modules.logistics.FrequencyHandler.Frequency;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHaveWireless {

	public Frequency getFrequencyFirst();
	public Frequency getFrequencyLast();
	public World getWorld();
	public BlockPos getPos();
	
	public default boolean isLoaded() {
		return getWorld().isBlockPresent(getPos());
	}
	
}
