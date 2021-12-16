package com.simibubi.create.lib.mixin.accessor;

import net.minecraft.world.level.block.LiquidBlock;

import net.minecraft.world.level.material.FlowingFluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LiquidBlock.class)
public interface LiquidBlockAccessor {
	@Accessor("fluid")
	FlowingFluid create$getFluid();
}
