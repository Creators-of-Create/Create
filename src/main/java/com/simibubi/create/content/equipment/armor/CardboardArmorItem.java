package com.simibubi.create.content.equipment.armor;

import com.simibubi.create.Create;

import net.minecraft.world.item.equipment.ArmorType;

public class CardboardArmorItem extends BaseArmorItem {
	public CardboardArmorItem(ArmorType type, Properties properties) {
		super(AllArmorMaterials.CARDBOARD, type, properties, Create.asResource("cardboard"));
	}
}
