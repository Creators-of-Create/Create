package com.simibubi.create.foundation.utility;

import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class LegacyFluidNbtBridge {
	private LegacyFluidNbtBridge() {}

	public static Tag saveOptional(FluidStack stack, HolderLookup.Provider registries) {
		return FluidStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
			.result()
			.orElseGet(CompoundTag::new);
	}

	public static CompoundTag saveOptionalCompound(FluidStack stack, HolderLookup.Provider registries) {
		Tag tag = saveOptional(stack, registries);
		return tag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
	}

	public static Optional<FluidStack> parse(HolderLookup.Provider registries, Tag tag) {
		return FluidStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
			.result();
	}

	public static Optional<FluidStack> parse(HolderLookup.Provider registries, Optional<? extends Tag> tag) {
		return tag.flatMap(t -> parse(registries, t));
	}

	public static FluidStack parseOptional(HolderLookup.Provider registries, Tag tag) {
		return FluidStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
			.result()
			.orElse(FluidStack.EMPTY);
	}

	public static FluidStack parseOptional(HolderLookup.Provider registries, Optional<? extends Tag> tag) {
		return tag.map(t -> parseOptional(registries, t))
			.orElse(FluidStack.EMPTY);
	}

	public static CompoundTag serializeTank(FluidTank tank, HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		tank.serialize(output);
		return output.buildResult();
	}

	public static void deserializeTank(FluidTank tank, HolderLookup.Provider registries, CompoundTag tag) {
		tank.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
	}
}
