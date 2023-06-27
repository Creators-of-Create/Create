package com.simibubi.create.content.trains.track;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CurvedTrackDestroyPacket extends BlockEntityConfigurationPacket<TrackBlockEntity> {

	private BlockPos targetPos;
	private BlockPos soundSource;
	private boolean wrench;

	public CurvedTrackDestroyPacket(BlockPos pos, BlockPos targetPos, BlockPos soundSource, boolean wrench) {
		super(pos);
		this.targetPos = targetPos;
		this.soundSource = soundSource;
		this.wrench = wrench;
	}

	public CurvedTrackDestroyPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(targetPos);
		buffer.writeBlockPos(soundSource);
		buffer.writeBoolean(wrench);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		targetPos = buffer.readBlockPos();
		soundSource = buffer.readBlockPos();
		wrench = buffer.readBoolean();
	}

	@Override
	protected void applySettings(ServerPlayer player, TrackBlockEntity be) {
		int verifyDistance = AllConfigs.server().trains.maxTrackPlacementLength.get() * 4;
		if (!be.getBlockPos()
			.closerThan(player.blockPosition(), verifyDistance)) {
			Create.LOGGER.warn(player.getScoreboardName() + " too far away from destroyed Curve track");
			return;
		}

		Level level = be.getLevel();
		BezierConnection bezierConnection = be.getConnections()
			.get(targetPos);

		be.removeConnection(targetPos);
		if (level.getBlockEntity(targetPos)instanceof TrackBlockEntity other)
			other.removeConnection(pos);

		BlockState blockState = be.getBlockState();
		TrackPropagator.onRailRemoved(level, pos, blockState);

		if (wrench) {
			AllSoundEvents.WRENCH_REMOVE.playOnServer(player.level(), soundSource, 1,
				Create.RANDOM.nextFloat() * .5f + .5f);
			if (!player.isCreative() && bezierConnection != null) 
				bezierConnection.addItemsToPlayer(player);
		} else if (!player.isCreative() && bezierConnection != null)
			bezierConnection.spawnItems(level);

		bezierConnection.spawnDestroyParticles(level);
		SoundType soundtype = blockState.getSoundType(level, pos, player);
		if (soundtype == null)
			return;

		level.playSound(null, soundSource, soundtype.getBreakSound(), SoundSource.BLOCKS,
			(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
	}

	@Override
	protected int maxRange() {
		return 64;
	}

	@Override
	protected void applySettings(TrackBlockEntity be) {}

}
