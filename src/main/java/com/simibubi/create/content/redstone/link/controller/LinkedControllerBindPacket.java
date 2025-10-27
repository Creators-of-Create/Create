package com.simibubi.create.content.redstone.link.controller;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class LinkedControllerBindPacket extends LinkedControllerPacketBase {
	public static final StreamCodec<ByteBuf, LinkedControllerBindPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, p -> p.button,
			BlockPos.STREAM_CODEC, p -> p.linkLocation,
	        LinkedControllerBindPacket::new
	);

	private final int button;
	private final BlockPos linkLocation;

	public LinkedControllerBindPacket(int button, BlockPos linkLocation) {
		super(null);
		this.button = button;
		this.linkLocation = linkLocation;
	}

	@Override
	protected void handleItem(ServerPlayer player, ItemStack heldItem) {
		if (player.isSpectator())
			return;

		ItemStackHandler frequencyItems = LinkedControllerItem.getFrequencyItems(heldItem);
		LinkBehaviour linkBehaviour = BlockEntityBehaviour.get(player.level(), linkLocation, LinkBehaviour.TYPE);
		if (linkBehaviour == null)
			return;

		linkBehaviour.getNetworkKey()
			.forEachWithContext((f, first) -> frequencyItems.setStackInSlot(button * 2 + (first ? 0 : 1), f.getStack()
				.copy()));

		heldItem.set(AllDataComponents.LINKED_CONTROLLER_ITEMS, ItemHelper.containerContentsFromHandler(frequencyItems));
	}

	@Override
	protected void handleLectern(ServerPlayer player, LecternControllerBlockEntity lectern) {}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LINKED_CONTROLLER_BIND;
	}
}
