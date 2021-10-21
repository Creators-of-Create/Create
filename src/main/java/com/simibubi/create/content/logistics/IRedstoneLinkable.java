package com.simibubi.create.content.logistics;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler.Frequency;

import net.minecraft.util.math.BlockPos;

public interface IRedstoneLinkable {

	public int getTransmittedStrength();
	
	public void setReceivedStrength(int power);
	
	public boolean isListening();
	
	public boolean isAlive();
	
	public Pair<Frequency, Frequency> getNetworkKey();
	
	public BlockPos getLocation();
	
}
