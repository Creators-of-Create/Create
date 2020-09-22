package com.simibubi.create.content.contraptions.goggles;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

/*
* Implement this Interface in the TileEntity class that wants to add info to the screen
* */
public interface IHaveHoveringInformation {

	default boolean addToTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking){
		return false;
	}

}
