package com.simibubi.create.foundation.fluid;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;

@ScheduledForRemoval(inVersion = "1.21.1+ Port")
@Deprecated(since = "6.0.7", forRemoval = true)
public class FluidIngredientOld {
	private static final Codec<SizedFluidIngredient> FLUID_STACK = RecordCodecBuilder.create(i -> i.group(
			validatedType("fluid_stack"),
			FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("fluid").forGetter(s -> null),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(s -> null),
			Codec.INT.fieldOf("amount").forGetter(s -> null)
		).apply(i, (type, fluid, components, amount) -> new SizedFluidIngredient(DataComponentFluidIngredient.of(false, components.split().added(), fluid), amount))
	);

	private static final Codec<SizedFluidIngredient> FLUID_TAG = RecordCodecBuilder.create(i -> i.group(
		validatedType("fluid_tag"),
		TagKey.codec(Registries.FLUID).fieldOf("fluid_tag").forGetter(s -> null),
		Codec.INT.fieldOf("amount").forGetter(s -> null)
	).apply(i, (type, tag, amount) -> new SizedFluidIngredient(TagFluidIngredient.tag(tag), amount)));

	public static final Codec<SizedFluidIngredient> CODEC = Codec.withAlternative(FLUID_STACK, FLUID_TAG);

	private static <T> RecordCodecBuilder<T, String> validatedType(String requiredType) {
		return Codec.STRING
			.validate(s -> s.equals(requiredType) ? DataResult.success(s) : DataResult.error(() -> "Invalid Type: " + s))
			.fieldOf("type")
			.forGetter(s -> requiredType);
	}
}
