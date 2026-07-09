package com.simibubi.create.content.kinetics.transmission.sequencer;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class SequencedGearshiftClient {

	public static void displayScreen(SequencedGearshiftBlockEntity be, Player player) {
		if (player instanceof LocalPlayer)
			ScreenOpener.open(new SequencedGearshiftScreen(be));
	}

}
