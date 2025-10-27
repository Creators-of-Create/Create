package com.simibubi.create.content.processing.sequenced;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public interface IAssemblyRecipe {
	default boolean supportsAssembly() {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	Component getDescriptionForAssembly();

	void addRequiredMachines(Set<ItemLike> list);

	void addAssemblyIngredients(List<Ingredient> list);

	default void addAssemblyFluidIngredients(List<SizedFluidIngredient> list) {
	}

	Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory();
}
