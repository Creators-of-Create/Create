package com.simibubi.create.api.equipment.goggles;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

// TODO: 1.21.1+ - Move into api package
/**
 * Implement this interface on the {@link BlockEntity} that wants to add info to the hovering overlay
 */
public non-sealed interface IHaveHoveringInformation extends IHaveCustomOverlayIcon {
	/**
	 * This method will be called when looking at a {@link BlockEntity} that implements this interface
	 *
	 * @return {@code true} if the tooltip creation was successful and should be
	 * displayed, or {@code false} if the overlay should not be displayed
	 */
	default boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return false;
	}
}
