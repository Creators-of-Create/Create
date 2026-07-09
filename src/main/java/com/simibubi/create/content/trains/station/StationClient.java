package com.simibubi.create.content.trains.station;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class StationClient {

	public static void displayScreen(StationBlock block, StationBlockEntity be, Player player) {
		if (!(player instanceof LocalPlayer))
			return;
		GlobalStation station = be.getStation();
		BlockState blockState = be.getBlockState();
		if (station == null || blockState == null)
			return;
		boolean assembling = blockState.getBlock() == block && blockState.getValue(StationBlock.ASSEMBLING);
		ScreenOpener.open(assembling ? new AssemblyScreen(be, station) : new StationScreen(be, station));
	}

}
