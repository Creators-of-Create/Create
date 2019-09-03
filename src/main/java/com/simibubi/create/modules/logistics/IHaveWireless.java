package com.simibubi.create.modules.logistics;

import com.simibubi.create.Create;
import com.simibubi.create.modules.logistics.FrequencyHandler.Frequency;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHaveWireless {

	public Frequency getFrequencyFirst();
	public Frequency getFrequencyLast();
	public void setFrequency(boolean first, ItemStack stack);
	public World getWorld();
	public BlockPos getPos();
	
	public default boolean isLoaded() {
		return getWorld().isBlockPresent(getPos());
	}
	default FrequencyHandler getHandler() {
		return Create.frequencyHandler;
	}
	
}
