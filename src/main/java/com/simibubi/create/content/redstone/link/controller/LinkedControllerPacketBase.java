package com.simibubi.create.content.redstone.link.controller;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public abstract class LinkedControllerPacketBase extends SimplePacketBase {

	private BlockPos lecternPos;

	public LinkedControllerPacketBase(BlockPos lecternPos) {
		this.lecternPos = lecternPos;
	}

	public LinkedControllerPacketBase(FriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			lecternPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
		}
	}

	protected boolean inLectern() {
		return lecternPos != null;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(inLectern());
		if (inLectern()) {
			buffer.writeInt(lecternPos.getX());
			buffer.writeInt(lecternPos.getY());
			buffer.writeInt(lecternPos.getZ());
		}
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;

			if (inLectern()) {
				BlockEntity be = player.level().getBlockEntity(lecternPos);
				if (!(be instanceof LecternControllerBlockEntity))
					return;
				handleLectern(player, (LecternControllerBlockEntity) be);
			} else {
				ItemStack controller = player.getMainHandItem();
				if (!AllItems.LINKED_CONTROLLER.isIn(controller)) {
					controller = player.getOffhandItem();
					if (!AllItems.LINKED_CONTROLLER.isIn(controller))
						return;
				}
				handleItem(player, controller);
			}
		});
		return true;
	}

	protected abstract void handleItem(ServerPlayer player, ItemStack heldItem);
	protected abstract void handleLectern(ServerPlayer player, LecternControllerBlockEntity lectern);

}
