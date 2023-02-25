package com.simibubi.create.content.logistics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LinkedControllerStopLecternPacket extends LinkedControllerPacketBase {

	public LinkedControllerStopLecternPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	public LinkedControllerStopLecternPacket(BlockPos lecternPos) {
		super(lecternPos);
	}

	@Override
	protected void handleLectern(ServerPlayer player, LecternControllerBlockEntity lectern) {
		lectern.tryStopUsing(player);
	}

	@Override
	protected void handleItem(ServerPlayer player, ItemStack heldItem) { }

}
