package com.simibubi.create.foundation.utility.recipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public interface IRecipeTypeInfo {

	ResourceLocation getId();

	<T extends IRecipeSerializer<?>> T getSerializer();

	<T extends IRecipeType<?>> T getType();

}
