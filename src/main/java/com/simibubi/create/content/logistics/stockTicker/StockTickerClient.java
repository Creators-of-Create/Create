package com.simibubi.create.content.logistics.stockTicker;

import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class StockTickerClient {

	public static void refreshStockSnapshot(BlockPos pos) {
		ClientNetworkHelper.INSTANCE.sendToServer(new LogisticalStockRequestPacket(pos));
	}

	public static boolean mayAdministrate(StockTickerBlockEntity blockEntity) {
		Player player = Minecraft.getInstance().player;
		return player != null && blockEntity.behaviour.mayAdministrate(player);
	}

}
