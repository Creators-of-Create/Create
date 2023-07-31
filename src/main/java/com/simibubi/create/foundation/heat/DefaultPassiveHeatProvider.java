package com.simibubi.create.foundation.heat;

import com.simibubi.create.api.heat.IHeatProvider;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public interface DefaultPassiveHeatProvider extends IHeatProvider {
	@Override
	default HeatLevel getHeatLevel(Level level, BlockPos providerPos, BlockPos consumerPos){
		return HeatLevel.SMOULDERING;
	}

	@Override
	default BoundingBox getHeatedArea(Level level, BlockPos providerPos) {
		return new BoundingBox(providerPos.above());
	}
}
