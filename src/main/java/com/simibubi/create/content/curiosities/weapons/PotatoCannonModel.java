package com.simibubi.create.content.curiosities.weapons;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class PotatoCannonModel extends CustomRenderedItemModel {

	public PotatoCannonModel(IBakedModel template) {
		super(template, "potato_cannon");
		addPartials("cog");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new PotatoCannonItemRenderer();
	}

}
