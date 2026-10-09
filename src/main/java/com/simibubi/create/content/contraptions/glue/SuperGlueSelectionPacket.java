package com.simibubi.create.content.contraptions.glue;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public record SuperGlueSelectionPacket(BlockPos from, BlockPos to) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, SuperGlueSelectionPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SuperGlueSelectionPacket::from,
			BlockPos.STREAM_CODEC, SuperGlueSelectionPacket::to,
			SuperGlueSelectionPacket::new
	);

	@Override
	public void handle(ServerPlayer player) {
		if (!player.canInteractWithBlock(to, 2))
			return;
		if (!to.closerThan(from, 25))
			return;

		if (!SuperGlueSelectionHelper.isGlueGroupConnected(player.level(), from, to, false))
			return;
		if (!SuperGlueSelectionHelper.collectGlueFromInventory(player, 1, true))
			return;

		AABB bb = SuperGlueEntity.span(from, to);
		SuperGlueSelectionHelper.collectGlueFromInventory(player, 1, false);
		SuperGlueEntity entity = new SuperGlueEntity(player.level(), bb);
		player.level().addFreshEntity(entity);
		entity.spawnParticles();

		AllAdvancements.SUPER_GLUE.awardTo(player);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.GLUE_IN_AREA;
	}
}
