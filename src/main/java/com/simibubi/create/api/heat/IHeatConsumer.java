package com.simibubi.create.api.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IHeatConsumer {
	void onHeatProvided(Level level, IHeatProvider heatProvider, BlockPos heatProviderPos, BlockPos consumerPos);

	boolean isValidSource(IHeatProvider provider);
}
