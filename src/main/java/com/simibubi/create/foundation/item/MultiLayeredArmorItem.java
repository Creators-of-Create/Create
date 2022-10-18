package com.simibubi.create.foundation.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;

/**
 * This interface is meant to be implemented on {@link ArmorItem}s, which will allow them to be rendered on both the inner model and outer model.
 *
 * <p>Classes implementing this interface <b>must not</b> also implement {@link DyeableLeatherItem}.
 *
 * <p>Classes that implement this interface and override {@link IForgeItem#getArmorTexture(ItemStack, Entity, EquipmentSlot, String) getArmorTexture}
 * must note that the {@code String} argument will be used for layer context instead of the type.
 * This string will always be {@code "1"} when querying the location for the outer model or {@code "2"} when querying the location for the inner model.
 */
public interface MultiLayeredArmorItem {
}
