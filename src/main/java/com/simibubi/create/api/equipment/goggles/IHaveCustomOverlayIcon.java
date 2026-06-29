package com.simibubi.create.api.equipment.goggles;

import com.simibubi.create.AllItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

// TODO: 1.21.1+ - Move into api package
public sealed interface IHaveCustomOverlayIcon permits IHaveGoggleInformation, IHaveHoveringInformation {
	/**
	 * This method will be called when looking at a {@link BlockEntity} that implements {@link IHaveGoggleInformation}
	 * or {@link IHaveHoveringInformation}
	 *
	 * @return The {@link ItemStack} you want the overlay to show instead of the goggles
	 */
	default ItemStack getIcon(boolean isPlayerSneaking) {
		return AllItems.GOGGLES.asStack();
	}
}
