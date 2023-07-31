package com.simibubi.create.api.heat;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Interface used by heat producing objects.
 * <p>
 * All {@link IHeatProvider} must register themselves when being "activated" to the SavedHeatData
 */
public interface IHeatProvider {
	/**
	 * Get the current {@link HeatLevel}
	 *
	 * @param level       Level the {@link IHeatProvider} is in
	 * @param providerPos Position of the {@link IHeatProvider}
	 * @param consumerPos Position of the heat-consuming block
	 */
	HeatLevel getHeatLevel(Level level, BlockPos providerPos, BlockPos consumerPos);

	/**
	 * Get the max range this block can heat
	 *
	 * @param level       Level the {@link IHeatProvider} is in
	 * @param providerPos Position of the {@link IHeatProvider}
	 */
	BoundingBox getHeatedArea(Level level, BlockPos providerPos);

	/**
	 * Allows you to check if the heat-consuming block is in range to get heated by this {@link IHeatProvider}
	 *
	 * @param level       Level the {@link IHeatProvider} is in
	 * @param providerPos Position of the {@link IHeatProvider}
	 * @param consumerPos Position of the heat-consuming block
	 */
	default boolean isInHeatRange(Level level, BlockPos providerPos, BlockPos consumerPos) {
		return getHeatedArea(level, providerPos).isInside(consumerPos);
	}

	/**
	 * Returns the max amount of heat-consuming blocks.
	 *
	 * @param level       Level the {@link IHeatProvider} is in
	 * @param providerPos Position of the {@link IHeatProvider}
	 */
	default int getMaxHeatConsumers(Level level, BlockPos providerPos) {
		return 1;
	}
}
