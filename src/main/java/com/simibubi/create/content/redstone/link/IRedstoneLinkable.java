package com.simibubi.create.content.redstone.link;

import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;

import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;

public interface IRedstoneLinkable {

	public int getTransmittedStrength();

	public void setReceivedStrength(int power);

	public int getReceivedStrength();

	public boolean isListening();

	public boolean isAlive();

	public Couple<Frequency> getNetworkKey();

	public BlockPos getLocation();

}
