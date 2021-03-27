package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.content.contraptions.base.IRotate;
import net.minecraft.block.BlockState;

public interface ICogWheel extends IRotate {
	static boolean isSmallCog(BlockState state) {
		return state.getBlock() instanceof ICogWheel && ((ICogWheel) state.getBlock()).isSmallCog();
	}

	static boolean isLargeCog(BlockState state) {
		return state.getBlock() instanceof ICogWheel && ((ICogWheel) state.getBlock()).isLargeCog();
	}

	default boolean isLargeCog() {
		return false;
	}

	default boolean isSmallCog() {
		return !isLargeCog();
	}
}
