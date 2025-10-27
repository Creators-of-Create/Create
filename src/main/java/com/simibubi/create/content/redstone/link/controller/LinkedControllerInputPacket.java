package com.simibubi.create.content.redstone.link.controller;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.simibubi.create.AllPackets;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LinkedControllerInputPacket extends LinkedControllerPacketBase {
	public static final StreamCodec<ByteBuf, LinkedControllerInputPacket> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecBuilders.list(ByteBufCodecs.INT), p -> p.activatedButtons,
			ByteBufCodecs.BOOL, p -> p.press,
			CatnipStreamCodecs.NULLABLE_BLOCK_POS, LinkedControllerPacketBase::getLecternPos,
	        LinkedControllerInputPacket::new
	);

	private final List<Integer> activatedButtons;
	private final boolean press;

	public LinkedControllerInputPacket(Collection<Integer> activatedButtons, boolean press) {
		this(activatedButtons, press, null);
	}

	public LinkedControllerInputPacket(Collection<Integer> activatedButtons, boolean press, BlockPos lecternPos) {
		super(lecternPos);
		this.activatedButtons = List.copyOf(activatedButtons);
		this.press = press;
	}

	@Override
	protected void handleLectern(ServerPlayer player, LecternControllerBlockEntity lectern) {
		if (lectern.isUsedBy(player))
			handleItem(player, lectern.getController());
	}

	@Override
	protected void handleItem(ServerPlayer player, ItemStack heldItem) {
		Level world = player.getCommandSenderWorld();
		UUID uniqueID = player.getUUID();
		BlockPos pos = player.blockPosition();

		if (player.isSpectator() && press)
			return;

		LinkedControllerServerHandler.receivePressed(world, pos, uniqueID, activatedButtons.stream()
			.map(i -> LinkedControllerItem.toFrequency(heldItem, i))
			.collect(Collectors.toList()), press);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LINKED_CONTROLLER_INPUT;
	}
}
