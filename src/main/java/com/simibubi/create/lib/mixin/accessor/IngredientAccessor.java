package com.simibubi.create.lib.mixin.accessor;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.crafting.Ingredient;

@Mixin(Ingredient.class)
public interface IngredientAccessor {
	@Accessor("values")
	Ingredient.Value[] getAcceptedItems();

	@Invoker("fromValues")
	static Ingredient invokeFromValues(Stream<? extends Ingredient.Value> stream) {
		throw new RuntimeException("mixin application failed");
	}
}
