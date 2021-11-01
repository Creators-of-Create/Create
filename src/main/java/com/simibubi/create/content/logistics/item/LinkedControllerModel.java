package com.simibubi.create.content.logistics.item;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.resources.model.BakedModel;

public class LinkedControllerModel extends CreateCustomRenderedItemModel {

	public LinkedControllerModel(BakedModel template) {
		super(template, "linked_controller");
		addPartials("powered", "button");
	}

}
