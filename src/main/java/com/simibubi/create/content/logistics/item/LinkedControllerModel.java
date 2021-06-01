package com.simibubi.create.content.logistics.item;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class LinkedControllerModel extends CustomRenderedItemModel {

	public LinkedControllerModel(IBakedModel template) {
		super(template, "linked_controller");
		addPartials("powered", "button");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new LinkedControllerItemRenderer();
	}

}
