package com.simibubi.create.content.contraptions.goggles;

import java.util.List;

import net.minecraft.network.chat.Component;

/*
* Implement this Interface in the TileEntity class that wants to add info to the screen
* */
public interface IHaveHoveringInformation {

	default boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return false;
	}

}
