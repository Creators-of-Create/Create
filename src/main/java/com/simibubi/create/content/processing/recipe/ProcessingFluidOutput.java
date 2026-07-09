package com.simibubi.create.content.processing.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;

public class ProcessingFluidOutput {

	public static final ProcessingFluidOutput EMPTY = new ProcessingFluidOutput(Fluids.EMPTY, 0);

	public static final Codec<ProcessingFluidOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(ProcessingFluidOutput::fluid),
		NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", 1000).forGetter(ProcessingFluidOutput::amount),
		DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ProcessingFluidOutput::patch)
	).apply(instance, ProcessingFluidOutput::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingFluidOutput> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.registry(Registries.FLUID), ProcessingFluidOutput::fluid,
		ByteBufCodecs.VAR_INT, ProcessingFluidOutput::amount,
		DataComponentPatch.STREAM_CODEC, ProcessingFluidOutput::patch,
		ProcessingFluidOutput::new
	);

	private final Fluid fluid;
	private final int amount;
	private final DataComponentPatch patch;

	public ProcessingFluidOutput(Fluid fluid, int amount) {
		this(fluid, amount, DataComponentPatch.EMPTY);
	}

	public ProcessingFluidOutput(Fluid fluid, int amount, DataComponentPatch patch) {
		this.fluid = fluid;
		this.amount = amount;
		this.patch = patch;
	}

	public ProcessingFluidOutput(FluidStack stack) {
		this(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch());
	}

	public FluidStack getStack() {
		return new FluidStack(fluid, amount, patch);
	}

	public Fluid fluid() {
		return fluid;
	}

	public int amount() {
		return amount;
	}

	public DataComponentPatch patch() {
		return patch;
	}
}
