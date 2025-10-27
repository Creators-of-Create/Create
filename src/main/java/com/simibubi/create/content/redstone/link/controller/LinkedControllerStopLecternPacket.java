package com.simibubi.create.content.redstone.link.controller;

import java.util.Objects;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LinkedControllerStopLecternPacket extends LinkedControllerPacketBase {
	public static final StreamCodec<ByteBuf, LinkedControllerStopLecternPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
			LinkedControllerStopLecternPacket::new, LinkedControllerPacketBase::getLecternPos
	);

	public LinkedControllerStopLecternPacket(BlockPos lecternPos) {
		super(Objects.requireNonNull(lecternPos));
	}

	@Override
	protected void handleLectern(ServerPlayer player, LecternControllerBlockEntity lectern) {
		lectern.tryStopUsing(player);
	}

	@Override
	protected void handleItem(ServerPlayer player, ItemStack heldItem) { }

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LINKED_CONTROLLER_USE_LECTERN;
	}
}
