package com.simibubi.create.content.contraptions.sync;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record ClientMotionPacket(Vec3 motion, boolean onGround, float limbSwing) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ClientMotionPacket> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecs.VEC3, ClientMotionPacket::motion,
			ByteBufCodecs.BOOL, ClientMotionPacket::onGround,
			ByteBufCodecs.FLOAT, ClientMotionPacket::limbSwing,
	        ClientMotionPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CLIENT_MOTION;
	}

	@Override
	public void handle(ServerPlayer sender) {
		if (sender == null)
			return;
		sender.setDeltaMovement(motion);
		sender.setOnGround(onGround);
		if (onGround) {
			sender.causeFallDamage(sender.fallDistance, 1, sender.damageSources().fall());
			sender.fallDistance = 0;
			sender.connection.aboveGroundTickCount = 0;
			sender.connection.aboveGroundVehicleTickCount = 0;
		}
		CatnipServices.NETWORK.sendToClientsTrackingEntity(sender,
				new LimbSwingUpdatePacket(sender.getId(), sender.position(), limbSwing));
	}
}
