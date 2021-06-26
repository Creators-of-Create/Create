package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class ExtendoGripModel extends CustomRenderedItemModel {

	public ExtendoGripModel(IBakedModel template) {
		super(template, "extendo_grip");
		addPartials("cog", "thin_short", "wide_short", "thin_long", "wide_long");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new ExtendoGripItemRenderer();
	}

}
