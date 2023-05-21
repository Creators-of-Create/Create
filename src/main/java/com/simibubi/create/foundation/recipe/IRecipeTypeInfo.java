package com.simibubi.create.foundation.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public interface IRecipeTypeInfo {

	ResourceLocation getId();

	<T extends RecipeSerializer<?>> T getSerializer();

	<T extends RecipeType<?>> T getType();

}
