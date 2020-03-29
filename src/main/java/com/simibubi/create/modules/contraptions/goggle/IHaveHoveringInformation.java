package com.simibubi.create.modules.contraptions.goggle;

import java.util.List;

/*
* Implement this Interface in the TileEntity class that wants to add info to the screen
* */
public interface IHaveHoveringInformation {

	default boolean addToTooltip(List<String> tooltip, boolean isPlayerSneaking){
		return false;
	}

}
