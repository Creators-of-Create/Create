package com.simibubi.create.content.logistics.item;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

public class LinkedControllerModel extends CreateCustomRenderedItemModel {

	public LinkedControllerModel(BakedModel template) {
		super(template, "linked_controller");
		addPartials("powered", "button");
	}

	@Override
	public BlockEntityWithoutLevelRenderer createRenderer() {
		return new LinkedControllerItemRenderer();
	}

}
