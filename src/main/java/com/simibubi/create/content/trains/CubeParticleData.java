package com.simibubi.create.content.trains;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleData;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CubeParticleData implements ParticleOptions, ICustomParticleData<CubeParticleData> {

	public static final MapCodec<CubeParticleData> CODEC = RecordCodecBuilder.mapCodec(i ->
		i.group(
				Codec.FLOAT.fieldOf("r").forGetter(p -> p.r),
				Codec.FLOAT.fieldOf("g").forGetter(p -> p.g),
				Codec.FLOAT.fieldOf("b").forGetter(p -> p.b),
				Codec.FLOAT.fieldOf("scale").forGetter(p -> p.scale),
				Codec.INT.fieldOf("avg_age").forGetter(p -> p.avgAge),
				Codec.BOOL.fieldOf("hot").forGetter(p -> p.hot))
			.apply(i, CubeParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CubeParticleData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, p -> p.r,
			ByteBufCodecs.FLOAT, p -> p.g,
			ByteBufCodecs.FLOAT, p -> p.b,
			ByteBufCodecs.FLOAT, p -> p.scale,
			ByteBufCodecs.INT, p -> p.avgAge,
			ByteBufCodecs.BOOL, p -> p.hot,
			CubeParticleData::new
	);

	final float r;
	final float g;
	final float b;
	final float scale;
	final int avgAge;
	final boolean hot;

	public CubeParticleData(float r, float g, float b, float scale, int avgAge, boolean hot) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.scale = scale;
		this.avgAge = avgAge;
		this.hot = hot;
	}

	public CubeParticleData() {
		this(0, 0, 0, 0, 0, false);
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, CubeParticleData> getStreamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public MapCodec<CubeParticleData> getCodec(ParticleType<CubeParticleData> type) {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ParticleProvider<CubeParticleData> getFactory() {
		return new CubeParticle.Factory();
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.CUBE.get();
	}
}
