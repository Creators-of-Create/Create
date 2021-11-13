package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

@Mixin(BucketItem.class)
public interface BucketItemAccessor {
	@Accessor("content")
	Fluid getContent();
}
