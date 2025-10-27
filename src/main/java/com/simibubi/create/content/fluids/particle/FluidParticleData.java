package com.simibubi.create.content.fluids.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleData;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidParticleData implements ParticleOptions, ICustomParticleData<FluidParticleData> {

	private ParticleType<FluidParticleData> type;
	private FluidStack fluid;

	public FluidParticleData() {
	}

	@SuppressWarnings("unchecked")
	public FluidParticleData(ParticleType<?> type, FluidStack fluid) {
		this.type = (ParticleType<FluidParticleData>) type;
		this.fluid = fluid;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ParticleProvider<FluidParticleData> getFactory() {
		return (data, world, x, y, z, vx, vy, vz) -> FluidStackParticle.create(data.type, world, data.fluid, x, y, z,
			vx, vy, vz);
	}

	@Override
	public ParticleType<?> getType() {
		return type;
	}

	public static final MapCodec<FluidParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(FluidStack.CODEC.fieldOf("fluid")
			.forGetter(p -> p.fluid))
		.apply(i, fs -> new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), fs)));

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidParticleData> STREAM_CODEC = FluidStack.STREAM_CODEC
		.map(fs -> new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), fs), p -> p.fluid);


	public static final MapCodec<FluidParticleData> BASIN_CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(FluidStack.CODEC.fieldOf("fluid")
			.forGetter(p -> p.fluid))
		.apply(i, fs -> new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), fs)));

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidParticleData> BASIN_STREAM_CODEC = FluidStack.STREAM_CODEC
		.map(fs -> new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), fs), p -> p.fluid);

	public static final MapCodec<FluidParticleData> DRIP_CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(FluidStack.CODEC.fieldOf("fluid")
			.forGetter(p -> p.fluid))
		.apply(i, fs -> new FluidParticleData(AllParticleTypes.FLUID_DRIP.get(), fs)));

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidParticleData> DRIP_STREAM_CODEC = FluidStack.STREAM_CODEC
		.map(fs -> new FluidParticleData(AllParticleTypes.FLUID_DRIP.get(), fs), p -> p.fluid);

	@Override
	public MapCodec<FluidParticleData> getCodec(ParticleType<FluidParticleData> type) {
		if (type == AllParticleTypes.BASIN_FLUID.get())
			return BASIN_CODEC;
		if (type == AllParticleTypes.FLUID_DRIP.get())
			return DRIP_CODEC;
		return CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, FluidParticleData> getStreamCodec() {
		if (type == AllParticleTypes.BASIN_FLUID.get())
			return BASIN_STREAM_CODEC;
		if (type == AllParticleTypes.FLUID_DRIP.get())
			return DRIP_STREAM_CODEC;
		return STREAM_CODEC;
	}
}
