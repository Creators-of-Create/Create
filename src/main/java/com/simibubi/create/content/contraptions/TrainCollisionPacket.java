package com.simibubi.create.content.contraptions;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.damageTypes.CreateDamageSources;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record TrainCollisionPacket(int damage, int contraptionEntityId) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, TrainCollisionPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, TrainCollisionPacket::damage,
			ByteBufCodecs.INT, TrainCollisionPacket::contraptionEntityId,
	        TrainCollisionPacket::new
	);

	@Override
	public void handle(ServerPlayer player) {
		Level level = player.level();

		Entity entity = level.getEntity(contraptionEntityId);
		if (!(entity instanceof CarriageContraptionEntity cce))
			return;

		player.hurt(CreateDamageSources.runOver(level, cce), damage);
		player.level().playSound(player, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
				1, .75f);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TRAIN_COLLISION;
	}
}
