package com.simibubi.create.content.trains.track;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CurvedTrackDestroyPacket extends BlockEntityConfigurationPacket<TrackBlockEntity> {
	public static final StreamCodec<ByteBuf, CurvedTrackDestroyPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, packet -> packet.pos,
			BlockPos.STREAM_CODEC, packet -> packet.targetPos,
			BlockPos.STREAM_CODEC, packet -> packet.soundSource,
			ByteBufCodecs.BOOL, packet -> packet.wrench,
			CurvedTrackDestroyPacket::new
	);

	private final BlockPos targetPos;
	private final BlockPos soundSource;
	private final boolean wrench;

	public CurvedTrackDestroyPacket(BlockPos pos, BlockPos targetPos, BlockPos soundSource, boolean wrench) {
		super(pos);
		this.targetPos = targetPos;
		this.soundSource = soundSource;
		this.wrench = wrench;
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
		return AllConfigs.server().trains.maxTrackPlacementLength.get() + 16;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.DESTROY_CURVED_TRACK;
	}
}
