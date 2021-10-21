package com.simibubi.create.content.logistics.item;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;

public class LinkedControllerModel extends CreateCustomRenderedItemModel {

	public LinkedControllerModel(IBakedModel template) {
		super(template, "linked_controller");
		addPartials("powered", "button");
	}

}
