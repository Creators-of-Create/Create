package com.simibubi.create.infrastructure.command;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSpecialTextures;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.api.client.outliner.Outliner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.shapes.Shapes;

import net.neoforged.api.distmarker.Dist;

public record HighlightPacket(BlockPos pos) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, HighlightPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(HighlightPacket::new, p -> p.pos);

	@Override
	public void handle(Player player) {
		if (!player.level().hasChunkAt(pos)) {
			return;
		}

		Outliner.getInstance().showAABB("highlightCommand", Shapes.block().bounds().move(pos), 200)
			.lineWidth(1 / 32f)
			.colored(0xEeEeEe)
			// .colored(0x243B50)
			.withFaceTexture(AllSpecialTextures.SELECTION);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.BLOCK_HIGHLIGHT;
	}
}
