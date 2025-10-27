package com.simibubi.create.content.logistics.redstoneRequester;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record RedstoneRequesterEffectPacket(BlockPos pos, boolean success) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, RedstoneRequesterEffectPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, RedstoneRequesterEffectPacket::pos,
		ByteBufCodecs.BOOL, RedstoneRequesterEffectPacket::success,
	    RedstoneRequesterEffectPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.REDSTONE_REQUESTER_EFFECT;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof RedstoneRequesterBlockEntity plbe)
			plbe.playEffect(success);
	}
}
