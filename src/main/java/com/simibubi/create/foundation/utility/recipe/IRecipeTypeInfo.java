package com.simibubi.create.foundation.utility.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;

public interface IRecipeTypeInfo {

	ResourceLocation getId();

	<T extends RecipeSerializer<?>> T getSerializer();

	<T extends RecipeType<?>> T getType();

}
