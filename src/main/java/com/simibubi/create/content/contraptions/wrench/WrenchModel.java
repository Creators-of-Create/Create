package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class WrenchModel extends CustomRenderedItemModel {

	public WrenchModel(IBakedModel template) {
		super(template, "wrench");
		addPartials("gear");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new WrenchItemRenderer();
	}

}
