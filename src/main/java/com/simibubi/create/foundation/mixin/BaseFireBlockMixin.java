package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.api.heat.IHeatProvider;
import com.simibubi.create.foundation.heat.DefaultPassiveHeatProvider;

import net.minecraft.world.level.block.BaseFireBlock;

/**
 * Make {@link BaseFireBlock} implement an {@link IHeatProvider}
 */
@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin implements DefaultPassiveHeatProvider {
}
