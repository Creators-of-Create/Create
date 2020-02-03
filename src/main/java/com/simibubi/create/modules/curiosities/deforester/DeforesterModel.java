package com.simibubi.create.modules.curiosities.deforester;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class DeforesterModel extends CustomRenderedItemModel {

	public DeforesterModel(IBakedModel template) {
		super(template, "deforester");
		addPartials("gear", "light", "blade");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new DeforesterItemRenderer();
	}

}
