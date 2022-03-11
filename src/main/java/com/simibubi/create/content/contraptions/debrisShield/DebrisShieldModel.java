package com.simibubi.create.content.contraptions.debrisShield;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.resources.model.BakedModel;

public class DebrisShieldModel extends CreateCustomRenderedItemModel {

	public DebrisShieldModel(BakedModel template) {
		super(template, "debris_shield");
		addPartials("gear");
	}

}
