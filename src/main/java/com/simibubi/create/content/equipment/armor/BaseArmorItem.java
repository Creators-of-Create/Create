package com.simibubi.create.content.equipment.armor;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class BaseArmorItem extends Item {
	protected final Identifier textureLoc;

	public BaseArmorItem(ArmorMaterial armorMaterial, ArmorType type, Properties properties, Identifier textureLoc) {
		super(properties.stacksTo(1)
			.humanoidArmor(armorMaterial, type));
		this.textureLoc = textureLoc;
	}
}
