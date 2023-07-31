package com.simibubi.create.api.heat;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public interface IHeatConsumer {
	void onHeatProvided(Level level, IHeatProvider heatProvider, BlockPos heatProviderPos, BlockPos consumerPos);

	boolean isValidSource(Level level, IHeatProvider provider, BlockPos providerPos, BlockPos consumerPos);

	default Optional<HeatHandler> getHeatHandler(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return Optional.of(HeatHandler.load(serverLevel));
		}

		return Optional.empty();
	}

	default void addHeatConsumer(Level level, BlockPos pos) {
		getHeatHandler(level).ifPresent(heatHandler -> heatHandler.addHeatConsumer(pos, this));
	}

	default void removeHeatConsumer(Level level, BlockPos pos) {
		getHeatHandler(level).ifPresent(heatHandler -> heatHandler.removeHeatConsumer(pos));
	}
}
