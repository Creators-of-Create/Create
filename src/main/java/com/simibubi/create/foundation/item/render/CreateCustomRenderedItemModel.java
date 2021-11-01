package com.simibubi.create.foundation.item.render;

import com.simibubi.create.Create;

import net.minecraft.client.resources.model.BakedModel;

public abstract class CreateCustomRenderedItemModel extends CustomRenderedItemModel {

	public CreateCustomRenderedItemModel(BakedModel template, String basePath) {
		super(template, Create.ID, basePath);
	}

}
