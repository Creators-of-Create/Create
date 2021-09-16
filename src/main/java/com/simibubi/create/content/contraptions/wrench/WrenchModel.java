package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

public class WrenchModel extends CreateCustomRenderedItemModel {

	public WrenchModel(BakedModel template) {
		super(template, "wrench");
		addPartials("gear");
	}

	@Override
	public BlockEntityWithoutLevelRenderer createRenderer() {
		return new WrenchItemRenderer();
	}

}
