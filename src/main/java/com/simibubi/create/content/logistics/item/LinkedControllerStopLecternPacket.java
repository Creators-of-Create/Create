package com.simibubi.create.content.logistics.item;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class LinkedControllerStopLecternPacket extends LinkedControllerPacketBase {

	public LinkedControllerStopLecternPacket(PacketBuffer buffer) {
		super(buffer);
	}

	public LinkedControllerStopLecternPacket(BlockPos lecternPos) {
		super(lecternPos);
	}

	@Override
	protected void handleLectern(ServerPlayerEntity player, LecternControllerTileEntity lectern) {
		lectern.tryStopUsing(player);
	}

	@Override
	protected void handleItem(ServerPlayerEntity player, ItemStack heldItem) { }

}
