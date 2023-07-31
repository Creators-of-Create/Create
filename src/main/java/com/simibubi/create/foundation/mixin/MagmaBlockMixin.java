package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.foundation.heat.DefaultPassiveHeatProvider;

import net.minecraft.world.level.block.MagmaBlock;

@Mixin(MagmaBlock.class)
public class MagmaBlockMixin implements DefaultPassiveHeatProvider {
}
