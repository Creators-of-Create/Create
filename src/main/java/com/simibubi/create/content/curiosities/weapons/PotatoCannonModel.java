package com.simibubi.create.content.curiosities.weapons;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.resources.model.BakedModel;

public class PotatoCannonModel extends CreateCustomRenderedItemModel {

	public PotatoCannonModel(BakedModel template) {
		super(template, "potato_cannon");
		addPartials("cog");
	}

}
