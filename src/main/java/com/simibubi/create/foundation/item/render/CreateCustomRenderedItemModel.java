package com.simibubi.create.foundation.item.render;

import com.simibubi.create.Create;

import net.minecraft.client.renderer.model.IBakedModel;

public abstract class CreateCustomRenderedItemModel extends CustomRenderedItemModel {

	public CreateCustomRenderedItemModel(IBakedModel template, String basePath) {
		super(template, Create.ID, basePath);
	}

}
