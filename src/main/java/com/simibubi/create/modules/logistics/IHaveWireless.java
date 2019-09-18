package com.simibubi.create.modules.logistics;

import com.simibubi.create.Create;
import com.simibubi.create.modules.logistics.FrequencyHandler.Frequency;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHaveWireless {

	public Frequency getFrequencyFirst();
	public Frequency getFrequencyLast();
	public void setFrequency(boolean first, ItemStack stack);
	
	public default World getWirelessWorld() {
		return ((TileEntity) this).getWorld();
	}
	
	public default BlockPos getWirelessPos() {
		return ((TileEntity) this).getPos();
	}
	
	public default boolean isLoaded() {
		return getWirelessWorld().isBlockPresent(getWirelessPos());
	}
	
	default FrequencyHandler getHandler() {
		return Create.frequencyHandler;
	}
	
}
