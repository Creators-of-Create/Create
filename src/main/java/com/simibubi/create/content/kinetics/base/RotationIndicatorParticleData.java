package com.simibubi.create.content.kinetics.base;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RotationIndicatorParticleData
	implements ParticleOptions, ICustomParticleDataWithSprite<RotationIndicatorParticleData> {

	public static final MapCodec<RotationIndicatorParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(Codec.INT.fieldOf("color")
				.forGetter(p -> p.color),
			Codec.FLOAT.fieldOf("speed")
				.forGetter(p -> p.speed),
			Codec.FLOAT.fieldOf("radius1")
				.forGetter(p -> p.radius1),
			Codec.FLOAT.fieldOf("radius2")
				.forGetter(p -> p.radius2),
			Codec.INT.fieldOf("life_span")
				.forGetter(p -> p.lifeSpan),
			Axis.CODEC.fieldOf("axis")
				.forGetter(p -> p.axis))
		.apply(i, RotationIndicatorParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RotationIndicatorParticleData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, p -> p.color,
			ByteBufCodecs.FLOAT, p -> p.speed,
			ByteBufCodecs.FLOAT, p -> p.radius1,
			ByteBufCodecs.FLOAT, p -> p.radius2,
			ByteBufCodecs.INT, p -> p.lifeSpan,
			CatnipStreamCodecs.AXIS, p -> p.axis,
			RotationIndicatorParticleData::new
	);

	final int color;
	final float speed;
	final float radius1;
	final float radius2;
	final int lifeSpan;
	final Axis axis;

	public RotationIndicatorParticleData(int color, float speed, float radius1, float radius2, int lifeSpan,
										 Axis axis) {
		this.color = color;
		this.speed = speed;
		this.radius1 = radius1;
		this.radius2 = radius2;
		this.lifeSpan = lifeSpan;
		this.axis = axis;
	}

	public RotationIndicatorParticleData() {
		this(0, 0, 0, 0, 0, Axis.X);
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.ROTATION_INDICATOR.get();
	}

	public Axis getAxis() {
		return axis;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, RotationIndicatorParticleData> getStreamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public MapCodec<RotationIndicatorParticleData> getCodec(ParticleType<RotationIndicatorParticleData> type) {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public SpriteParticleRegistration<RotationIndicatorParticleData> getMetaFactory() {
		return RotationIndicatorParticle.Factory::new;
	}

}
