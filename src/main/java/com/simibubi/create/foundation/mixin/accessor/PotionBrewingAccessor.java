package com.simibubi.create.foundation.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;

@Mixin(PotionBrewing.class)
public interface PotionBrewingAccessor {
	@Accessor("potionMixes")
	List<PotionBrewing.Mix<Potion>> create$getPotionMixes();

	@Accessor("containerMixes")
	List<PotionBrewing.Mix<Item>> create$getContainerMixes();

	@Invoker("isContainer")
	boolean create$isContainer(ItemStack stack);
}
