package com.simibubi.create.content.contraptions.glue;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record SuperGlueRemovalPacket(int entityId, BlockPos soundSource) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, SuperGlueRemovalPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, SuperGlueRemovalPacket::entityId,
			BlockPos.STREAM_CODEC, SuperGlueRemovalPacket::soundSource,
			SuperGlueRemovalPacket::new
	);

	@Override
	public void handle(ServerPlayer player) {
		Entity entity = player.level().getEntity(entityId);
		if (!(entity instanceof SuperGlueEntity superGlue))
			return;
		double range = 32;
		if (player.distanceToSqr(superGlue.position()) > range * range)
			return;
		AllSoundEvents.SLIME_ADDED.play(player.level(), null, soundSource, 0.5F, 0.5F);
		superGlue.spawnParticles();
		entity.discard();
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.GLUE_REMOVED;
	}
}
