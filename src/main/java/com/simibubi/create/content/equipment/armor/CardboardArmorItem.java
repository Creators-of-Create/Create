package com.simibubi.create.content.equipment.armor;

import com.simibubi.create.Create;

public class CardboardArmorItem extends BaseArmorItem {
	public CardboardArmorItem(Type type, Properties properties) {
		super(AllArmorMaterials.CARDBOARD, type, properties, Create.asResource("cardboard"));
	}
}
