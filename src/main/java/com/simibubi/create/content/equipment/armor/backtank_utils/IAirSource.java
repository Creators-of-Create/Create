package com.simibubi.create.content.equipment.armor.backtank_utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Base interface for the Air provider (such as Backtanks).
 * <p>
 * If you use Air Providers (such as implementing a tool),
 * you should use {@link com.simibubi.create.content.equipment.armor.BacktankUtil}
 * (For example, use {@link com.simibubi.create.content.equipment.armor.BacktankUtil#getAllWithAir(LivingEntity)}
 * to get player Air Sources )
 */
public interface IAirSource {
	float getAir();

//	int maxAir();

	void consumeAir(LivingEntity entity, float i);

	boolean hasAirRemaining();

	int getBarWidth();

	int getBarColor();

	boolean isFireResistant();

	ItemStack getDisplayedBacktank();
}
