package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.heat.DefaultPassiveHeatProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;

@Mixin(CampfireBlock.class)
public class CampfireMixin implements DefaultPassiveHeatProvider {
	@Override
	public HeatLevel getHeatLevel(Level level, BlockPos providerPos, BlockPos consumerPos) {
		if (CampfireBlock.isLitCampfire(level.getBlockState(providerPos))) {
			return DefaultPassiveHeatProvider.super.getHeatLevel(level, providerPos, consumerPos);
		}
		return HeatLevel.NONE;
	}
}
