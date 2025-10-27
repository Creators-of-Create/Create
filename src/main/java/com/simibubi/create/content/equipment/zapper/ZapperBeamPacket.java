package com.simibubi.create.content.equipment.zapper;

import com.simibubi.create.AllPackets;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.zapper.ZapperRenderHandler.LaserBeam;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ZapperBeamPacket extends ShootGadgetPacket {
	public static final StreamCodec<ByteBuf, ZapperBeamPacket> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecs.VEC3, packet -> packet.location,
			CatnipStreamCodecs.HAND, packet -> packet.hand,
			ByteBufCodecs.BOOL, packet -> packet.self,
			CatnipStreamCodecs.VEC3, packet -> packet.target,
			ZapperBeamPacket::new
	);

	private final Vec3 target;

	public ZapperBeamPacket(Vec3 start, InteractionHand hand, boolean self, Vec3 target) {
		super(start, hand, self);
		this.target = target;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected ShootableGadgetRenderHandler getHandler() {
		return CreateClient.ZAPPER_RENDER_HANDLER;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void handleAdditional() {
		CreateClient.ZAPPER_RENDER_HANDLER.addBeam(new LaserBeam(location, target));
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.BEAM_EFFECT;
	}
}
