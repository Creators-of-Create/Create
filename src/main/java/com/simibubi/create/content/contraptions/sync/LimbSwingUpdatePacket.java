package com.simibubi.create.content.contraptions.sync;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record LimbSwingUpdatePacket(int entityId, Vec3 position, float limbSwing) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, LimbSwingUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, LimbSwingUpdatePacket::entityId,
			CatnipStreamCodecs.VEC3, LimbSwingUpdatePacket::position,
			ByteBufCodecs.FLOAT, LimbSwingUpdatePacket::limbSwing,
	        LimbSwingUpdatePacket::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entity = player.clientLevel.getEntity(entityId);
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		data.putInt("LastOverrideLimbSwingUpdate", 0);
		data.putFloat("OverrideLimbSwing", limbSwing);
		entity.lerpTo(position.x, position.y, position.z, entity.getYRot(),
				entity.getXRot(), 2);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LIMBSWING_UPDATE;
	}
}
