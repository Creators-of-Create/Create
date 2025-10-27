package com.simibubi.create.content.contraptions.actors.trainControls;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ControlsInputPacket(List<Integer> activatedButtons, boolean press, int contraptionEntityId,
								  BlockPos controlsPos, boolean stopControlling) implements ServerboundPacketPayload {

	public static final StreamCodec<ByteBuf, ControlsInputPacket> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecBuilders.list(ByteBufCodecs.VAR_INT), ControlsInputPacket::activatedButtons,
			ByteBufCodecs.BOOL, ControlsInputPacket::press,
			ByteBufCodecs.INT, ControlsInputPacket::contraptionEntityId,
			BlockPos.STREAM_CODEC, ControlsInputPacket::controlsPos,
			ByteBufCodecs.BOOL, ControlsInputPacket::stopControlling,
	        ControlsInputPacket::new
	);

	public ControlsInputPacket(Collection<Integer> activatedButtons, boolean press, int contraptionEntityId, BlockPos controlsPos, boolean stopControlling) {
		// given list is reused, copy it
		this(List.copyOf(activatedButtons), press, contraptionEntityId, controlsPos, stopControlling);
	}

	@Override
	public void handle(ServerPlayer player) {
		Level world = player.getCommandSenderWorld();
		UUID uniqueID = player.getUUID();

		if (player.isSpectator() && press)
			return;

		Entity entity = world.getEntity(contraptionEntityId);
		if (!(entity instanceof AbstractContraptionEntity ace))
			return;
		if (stopControlling) {
			ace.stopControlling(controlsPos);
			return;
		}

		if (ace.toGlobalVector(Vec3.atCenterOf(controlsPos), 0)
				.closerThan(player.position(), 16))
			ControlsServerHandler.receivePressed(world, ace, controlsPos, uniqueID, activatedButtons, press);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTROLS_INPUT;
	}
}
