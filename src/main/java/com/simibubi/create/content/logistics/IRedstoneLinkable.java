package com.simibubi.create.content.logistics;

import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.BlockPos;

public interface IRedstoneLinkable {

	public int getTransmittedStrength();
	
	public void setReceivedStrength(int power);
	
	public boolean isListening();
	
	public boolean isAlive();
	
	public Couple<Frequency> getNetworkKey();
	
	public BlockPos getLocation();
	
}
