package com.simibubi.create.content.redstone.thresholdSwitch;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class ThresholdSwitchClient {

	public static void displayScreen(ThresholdSwitchBlockEntity be, Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener.open(new ThresholdSwitchScreen(be));
	}

}
