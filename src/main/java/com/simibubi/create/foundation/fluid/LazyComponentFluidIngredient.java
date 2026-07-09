package com.simibubi.create.foundation.fluid;

import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.recipe.AllIngredients;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;

public class LazyComponentFluidIngredient extends FluidIngredient {
	private static final Codec<HolderSet<Fluid>> FLUIDS_CODEC =
		HolderSetCodec.create(Registries.FLUID, BuiltInRegistries.FLUID.holderByNameCodec(), false);

	public static final MapCodec<LazyComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		FLUIDS_CODEC.fieldOf("fluids").forGetter(LazyComponentFluidIngredient::fluidSet),
		DataComponentExactPredicate.CODEC.fieldOf("components").forGetter(LazyComponentFluidIngredient::components),
		Codec.BOOL.optionalFieldOf("strict", false).forGetter(LazyComponentFluidIngredient::isStrict)
	).apply(i, LazyComponentFluidIngredient::new));

	private final HolderSet<Fluid> fluids;
	private final DataComponentExactPredicate components;
	private final boolean strict;

	public LazyComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentExactPredicate components, boolean strict) {
		this.fluids = fluids;
		this.components = components;
		this.strict = strict;
	}

	public static LazyComponentFluidIngredient of(boolean strict, FluidStack stack) {
		return of(strict, stack.getComponents(), HolderSet.direct(stack.typeHolder()));
	}

	public static LazyComponentFluidIngredient of(boolean strict, DataComponentMap components, HolderSet<Fluid> fluids) {
		return new LazyComponentFluidIngredient(fluids, DataComponentExactPredicate.allOf(components), strict);
	}

	@Override
	public boolean test(FluidStack stack) {
		if (!fluids.contains(stack.typeHolder()))
			return false;
		if (strict)
			return stack.getComponentsPatch().equals(components.asPatch());
		return components.test(stack);
	}

	@Override
	protected Stream<Holder<Fluid>> generateFluids() {
		return fluids.stream();
	}

	@Override
	public SlotDisplay display() {
		return new SlotDisplay.Composite(getStacks(1000).stream()
			.map(stack -> (SlotDisplay) new FluidStackSlotDisplay(stack))
			.toList());
	}

	public java.util.List<FluidStack> getStacks(int amount) {
		return fluids.stream()
			.map(fluid -> new FluidStack(fluid, amount, components.asPatch()))
			.toList();
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public FluidIngredientType<?> getType() {
		return AllIngredients.COMPONENT_FLUID_INGREDIENT.get();
	}

	public HolderSet<Fluid> fluidSet() {
		return fluids;
	}

	public DataComponentExactPredicate components() {
		return components;
	}

	public boolean isStrict() {
		return strict;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fluids, components, strict);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof LazyComponentFluidIngredient other))
			return false;
		return strict == other.strict && fluids.equals(other.fluids) && components.equals(other.components);
	}
}
