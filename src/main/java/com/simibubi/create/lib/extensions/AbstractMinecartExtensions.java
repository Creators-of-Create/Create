package com.simibubi.create.lib.extensions;

import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface AbstractMinecartExtensions {
	void create$moveMinecartOnRail(BlockPos pos);

	boolean create$canUseRail();

	BlockPos create$getCurrentRailPos();

	default float create$getMaxSpeedOnRail() {
		return 1.2f; // default in Forge
	}

	MinecartController create$getController();
}
