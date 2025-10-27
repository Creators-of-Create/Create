package com.simibubi.create.content.trains.track;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BezierTrackPointLocation(BlockPos curveTarget, int segment) {
	public static final Codec<BezierTrackPointLocation> CODEC = RecordCodecBuilder.create(i -> i.group(
		BlockPos.CODEC.fieldOf("curve_target").forGetter(BezierTrackPointLocation::curveTarget),
		Codec.INT.fieldOf("segment").forGetter(BezierTrackPointLocation::segment)
	).apply(i, BezierTrackPointLocation::new));

	public static final StreamCodec<ByteBuf, BezierTrackPointLocation> STREAM_CODEC = StreamCodec.composite(
	        BlockPos.STREAM_CODEC,
			BezierTrackPointLocation::curveTarget,
			ByteBufCodecs.INT,
			BezierTrackPointLocation::segment,
	        BezierTrackPointLocation::new
	);
}
