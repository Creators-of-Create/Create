package com.simibubi.create.content.contraptions.itemAssembly;

import java.util.List;
import java.util.Set;

import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public interface IAssemblyRecipe {

	default boolean supportsAssembly() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public Component getDescriptionForAssembly();

	public void addRequiredMachines(Set<ItemLike> list);

	public void addAssemblyIngredients(List<Ingredient> list);

	default void addAssemblyFluidIngredients(List<FluidIngredient> list) {}

//	public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory();

}
