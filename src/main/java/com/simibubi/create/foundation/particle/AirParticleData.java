package com.simibubi.create.foundation.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

public class AirParticleData implements ParticleOptions, ICustomParticleDataWithSprite<AirParticleData> {

	public static final MapCodec<AirParticleData> CODEC = RecordCodecBuilder.mapCodec(i ->
		i.group(
			Codec.FLOAT.fieldOf("drag").forGetter(p -> p.drag),
			Codec.FLOAT.fieldOf("speed").forGetter(p -> p.speed))
		.apply(i, AirParticleData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AirParticleData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, p -> p.drag,
			ByteBufCodecs.FLOAT, p -> p.speed,
			AirParticleData::new
	);

	float drag;
	float speed;

	public AirParticleData(float drag, float speed) {
		this.drag = drag;
		this.speed = speed;
	}

	public AirParticleData() {
		this(0, 0);
	}

	@Override
	public @NotNull ParticleType<?> getType() {
		return AllParticleTypes.AIR.get();
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, AirParticleData> getStreamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public MapCodec<AirParticleData> getCodec(ParticleType<AirParticleData> type) {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public SpriteParticleRegistration<AirParticleData> getMetaFactory() {
		return AirParticle.Factory::new;
	}

}
