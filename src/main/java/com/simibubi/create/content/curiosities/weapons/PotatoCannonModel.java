package com.simibubi.create.content.curiosities.weapons;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;

public class PotatoCannonModel extends CreateCustomRenderedItemModel {

	public PotatoCannonModel(IBakedModel template) {
		super(template, "potato_cannon");
		addPartials("cog");
	}

}
