package com.simibubi.create.lib.mixin.common.accessor;

import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PotionBrewing.class)
public interface PotionBrewingAccessor {
	@Accessor
	static List<Ingredient> getALLOWED_CONTAINERS() {
		throw new UnsupportedOperationException();
	}
}
