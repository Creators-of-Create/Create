package com.simibubi.create.lib.utility;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.function.Consumer;

public class NetworkUtil {

	public static void openGui(ServerPlayer player, MenuProvider containerProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
		player.openMenu(new ExtendedScreenHandlerFactory() {
			@Override
			public Component getDisplayName() {
				return containerProvider.getDisplayName();
			}

			@Override
			public AbstractContainerMenu createMenu(int arg0, Inventory arg1, Player arg2) {
				return containerProvider.createMenu(arg0, arg1, arg2);
			}

			@Override
			public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
				extraDataWriter.accept(buf);
			}
		});
	}
}
