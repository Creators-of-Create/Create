package com.simibubi.create.foundation.blockEntity.behaviour.scrollValue;

import net.minecraft.core.Direction;

public interface SidedScrollValueBehavior<B extends ScrollValueBehaviour>{

	/**
	 * Returns the ValueSettingsBehaviour for the given side.
	 *
	 * @param side the side for which to get the ValueSettingsBehaviour
	 * @return the ValueSettingsBehaviour for the given side
	 */
	B get(Direction side);
}
