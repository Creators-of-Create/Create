package com.simibubi.create.content.contraptions.sync;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;

public record LimbSwingUpdatePacket(int entityId, Vec3 position, float limbSwing) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, LimbSwingUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, LimbSwingUpdatePacket::entityId,
			CatnipStreamCodecs.VEC3, LimbSwingUpdatePacket::position,
			ByteBufCodecs.FLOAT, LimbSwingUpdatePacket::limbSwing,
	        LimbSwingUpdatePacket::new
	);

	@Override
	public void handle(Player player) {
		Entity entity = player.level().getEntity(entityId);
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		data.putInt("LastOverrideLimbSwingUpdate", 0);
		data.putFloat("OverrideLimbSwing", limbSwing);
		entity.snapTo(position, entity.getYRot(), entity.getXRot());
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LIMBSWING_UPDATE;
	}
}
