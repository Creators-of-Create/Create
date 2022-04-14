package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.TrackPropagator;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CurvedTrackDestroyPacket extends TileEntityConfigurationPacket<TrackTileEntity> {

	private BlockPos targetPos;
	private BlockPos soundSource;

	public CurvedTrackDestroyPacket(BlockPos pos, BlockPos targetPos, BlockPos soundSource) {
		super(pos);
		this.targetPos = targetPos;
		this.soundSource = soundSource;
	}

	public CurvedTrackDestroyPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(targetPos);
		buffer.writeBlockPos(soundSource);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		targetPos = buffer.readBlockPos();
		soundSource = buffer.readBlockPos();
	}

	@Override
	protected void applySettings(ServerPlayer player, TrackTileEntity te) {
		if (!te.getBlockPos()
			.closerThan(player.blockPosition(), 48)) {
			Create.LOGGER.warn(player.getScoreboardName() + " too far away from destroyed Curve track");
			return;
		}

		Level level = te.getLevel();
		te.removeConnection(targetPos);
		if (level.getBlockEntity(targetPos) instanceof TrackTileEntity other)
			other.removeConnection(pos);

		BlockState blockState = te.getBlockState();
		TrackPropagator.onRailRemoved(level, pos, blockState);

		SoundType soundtype = blockState.getSoundType(level, pos, player);
		if (soundtype == null)
			return;

		level.playSound(null, soundSource, soundtype.getBreakSound(), SoundSource.BLOCKS,
			(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
	}

	@Override
	protected void applySettings(TrackTileEntity te) {}

}
