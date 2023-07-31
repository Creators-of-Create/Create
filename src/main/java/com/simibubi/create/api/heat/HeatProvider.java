package com.simibubi.create.api.heat;

import java.util.Optional;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Interface used by heat producing objects.
 * <p>
 * All {@link HeatProvider} must register themselves when being "activated" to the SavedHeatData
 */
public interface HeatProvider {
	/**
	 * Get the current {@link HeatLevel}
	 *
	 * @param level       Level the {@link HeatProvider} is in
	 * @param providerPos Position of the {@link HeatProvider}
	 * @param consumerPos Position of the heat-consuming block
	 */
	HeatLevel getHeatLevel(Level level, BlockPos providerPos, BlockPos consumerPos);

	/**
	 * Get the max range this block can heat
	 *
	 * @param level       Level the {@link HeatProvider} is in
	 * @param providerPos Position of the {@link HeatProvider}
	 */
	BoundingBox getHeatedArea(Level level, BlockPos providerPos);

	/**
	 * Allows you to check if the heat-consuming block is in range to get heated by this {@link HeatProvider}
	 *
	 * @param level       Level the {@link HeatProvider} is in
	 * @param providerPos Position of the {@link HeatProvider}
	 * @param consumerPos Position of the heat-consuming block
	 */
	default boolean isInHeatRange(Level level, BlockPos providerPos, BlockPos consumerPos) {
		return getHeatedArea(level, providerPos).isInside(consumerPos);
	}

	/**
	 * Returns the max amount of heat-consuming blocks.
	 *
	 * @param level       Level the {@link HeatProvider} is in
	 * @param providerPos Position of the {@link HeatProvider}
	 */
	default int getMaxHeatConsumers(Level level, BlockPos providerPos) {
		return 1;
	}

	/**
	 * Returns the {@link HeatHandler} for this {@link Level} on Server Side or {@link Optional#empty()} on Client Side
	 */
	default Optional<HeatHandler> getHeatHandler(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return Optional.of(HeatHandler.load(serverLevel));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Registers the heat provider
	 */
	default void addHeatProvider(Level level, BlockPos pos) {
		getHeatHandler(level).ifPresent(heatHandler -> heatHandler.addHeatProvider(pos, this));
	}

	/**
	 * Removes the heat provider
	 */
	default void removeHeatProvider(Level level, BlockPos pos) {
		getHeatHandler(level).ifPresent(heatHandler -> heatHandler.removeHeatProvider(pos));
	}
}
