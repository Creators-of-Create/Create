package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.api.heat.HeatProvider;
import com.simibubi.create.foundation.heat.DefaultPassiveHeatProvider;

import net.minecraft.world.level.block.BaseFireBlock;

/**
 * Make {@link BaseFireBlock} implement an {@link HeatProvider}
 */
@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin implements DefaultPassiveHeatProvider {
}
