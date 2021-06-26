package com.simibubi.create.foundation.gui;

import net.minecraft.entity.player.PlayerEntity;

public interface IInteractionChecker {
	boolean canPlayerUse(PlayerEntity player);
}
