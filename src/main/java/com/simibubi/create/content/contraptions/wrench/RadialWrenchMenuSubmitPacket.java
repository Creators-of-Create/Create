package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record RadialWrenchMenuSubmitPacket(BlockPos blockPos, BlockState newState) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, RadialWrenchMenuSubmitPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, RadialWrenchMenuSubmitPacket::blockPos,
		CatnipStreamCodecs.BLOCK_STATE, RadialWrenchMenuSubmitPacket::newState,
	    RadialWrenchMenuSubmitPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.RADIAL_WRENCH_MENU_SUBMIT;
	}

	@Override
	public void handle(ServerPlayer player) {
		Level level = player.level();
		
		if (!level.getBlockState(blockPos).is(newState.getBlock()))
			return;

		BlockState updatedState = Block.updateFromNeighbourShapes(newState, level, blockPos);
		KineticBlockEntity.switchToBlockState(level, blockPos, updatedState);

		IWrenchable.playRotateSound(level, blockPos);
	}
}
