package com.simibubi.create.content.contraptions.fluids.particle;

import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.contraptions.particle.ICustomParticleData;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidParticleData implements IParticleData, ICustomParticleData<FluidParticleData> {

	private ParticleType<FluidParticleData> type;
	private FluidStack fluid;

	public FluidParticleData() {}

	@SuppressWarnings("unchecked")
	public FluidParticleData(ParticleType<?> type, FluidStack fluid) {
		this.type = (ParticleType<FluidParticleData>) type;
		this.fluid = fluid;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IParticleFactory<FluidParticleData> getFactory() {
		return (data, world, x, y, z, vx, vy, vz) -> FluidStackParticle.create(data.type, world, data.fluid, x, y, z,
			vx, vy, vz);
	}

	@Override
	public ParticleType<?> getType() {
		return type;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeFluidStack(fluid);
	}

	@Override
	public String getParameters() {
		return ForgeRegistries.PARTICLE_TYPES.getKey(type) + " " + fluid.getFluid()
			.getRegistryName();
	}

	public static final Codec<FluidStack> FLUID_CODEC = RecordCodecBuilder.create(i -> i.group(
		Registry.FLUID.fieldOf("FluidName")
			.forGetter(FluidStack::getFluid),
		Codec.INT.fieldOf("Amount")
			.forGetter(FluidStack::getAmount),
		CompoundNBT.CODEC.optionalFieldOf("tag")
			.forGetter((fs) -> {
				return Optional.ofNullable(fs.getTag());
			}))
		.apply(i, (f, a, t) -> new FluidStack(f, a, t.orElse(null))));

	public static final Codec<FluidParticleData> CODEC = RecordCodecBuilder.create(i -> i
		.group(FLUID_CODEC.fieldOf("fluid")
			.forGetter(p -> p.fluid))
		.apply(i, fs -> new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), fs)));

	public static final Codec<FluidParticleData> BASIN_CODEC = RecordCodecBuilder.create(i -> i
		.group(FLUID_CODEC.fieldOf("fluid")
			.forGetter(p -> p.fluid))
		.apply(i, fs -> new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), fs)));

	public static final Codec<FluidParticleData> DRIP_CODEC = RecordCodecBuilder.create(i -> i
		.group(FLUID_CODEC.fieldOf("fluid")
			.forGetter(p -> p.fluid))
		.apply(i, fs -> new FluidParticleData(AllParticleTypes.FLUID_DRIP.get(), fs)));

	public static final IParticleData.IDeserializer<FluidParticleData> DESERIALIZER =
		new IParticleData.IDeserializer<FluidParticleData>() {

			// TODO Fluid particles on command
			public FluidParticleData deserialize(ParticleType<FluidParticleData> particleTypeIn, StringReader reader)
				throws CommandSyntaxException {
				return new FluidParticleData(particleTypeIn, new FluidStack(Fluids.WATER, 1));
			}

			public FluidParticleData read(ParticleType<FluidParticleData> particleTypeIn, PacketBuffer buffer) {
				return new FluidParticleData(particleTypeIn, buffer.readFluidStack());
			}
		};

	@Override
	public IDeserializer<FluidParticleData> getDeserializer() {
		return DESERIALIZER;
	}

	@Override
	public Codec<FluidParticleData> getCodec(ParticleType<FluidParticleData> type) {
		if (type == AllParticleTypes.BASIN_FLUID.get())
			return BASIN_CODEC;
		if (type == AllParticleTypes.FLUID_DRIP.get())
			return DRIP_CODEC;
		return CODEC;
	}

}
