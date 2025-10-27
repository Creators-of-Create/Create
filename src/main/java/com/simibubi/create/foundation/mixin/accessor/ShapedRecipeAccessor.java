package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.world.item.crafting.ShapedRecipe;

import net.minecraft.world.item.crafting.ShapedRecipePattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {
	@Accessor("pattern")
	ShapedRecipePattern create$getPattern();
}
