package com.simibubi.create.content.redstone.link.controller;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllItems;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;


public abstract class LinkedControllerPacketBase implements ServerboundPacketPayload {
	@Nullable
	private final BlockPos lecternPos;

	public LinkedControllerPacketBase(@Nullable BlockPos lecternPos) {
		this.lecternPos = lecternPos;
	}

	@Nullable
	public BlockPos getLecternPos() {
		return lecternPos;
	}

	@Override
	public void handle(ServerPlayer player) {
		if (this.lecternPos != null) {
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
	}

	protected abstract void handleItem(ServerPlayer player, ItemStack heldItem);
	protected abstract void handleLectern(ServerPlayer player, LecternControllerBlockEntity lectern);
}
