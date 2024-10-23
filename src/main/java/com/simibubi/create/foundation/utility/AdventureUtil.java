package com.simibubi.create.foundation.utility;

import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

public class AdventureUtil {
	public static boolean isAdventure(@Nullable Player player) {
		return player != null && !player.mayBuild() && !player.isSpectator();
	}
}
