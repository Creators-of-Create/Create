package com.simibubi.create.content.equipment.symmetryWand;

import java.util.List;

import com.simibubi.create.AllPackets;

import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.api.platform.CatnipServices;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record SymmetryEffectPacket(BlockPos mirror, List<BlockPos> positions) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, SymmetryEffectPacket> STREAM_CODEC = StreamCodec.composite(
	        BlockPos.STREAM_CODEC, SymmetryEffectPacket::mirror,
			CatnipStreamCodecBuilders.list(BlockPos.STREAM_CODEC), SymmetryEffectPacket::positions,
	        SymmetryEffectPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.SYMMETRY_EFFECT;
	}

	@Override
	public void handle(Player player) {
		if (player.position().distanceTo(Vec3.atLowerCornerOf(mirror)) > 100)
			return;
		for (BlockPos to : positions)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> SymmetryWandClient.drawEffect(mirror, to));
	}
}
