package com.simibubi.create.content.contraptions.elevator;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class ElevatorContactClient {

	public static void displayScreen(ElevatorContactBlockEntity be, Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener.open(new ElevatorContactScreen(be.getBlockPos(), be.shortName, be.longName, be.doorControls.mode));
	}

}
