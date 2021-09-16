package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

public class ExtendoGripModel extends CreateCustomRenderedItemModel {

	public ExtendoGripModel(BakedModel template) {
		super(template, "extendo_grip");
		addPartials("cog", "thin_short", "wide_short", "thin_long", "wide_long");
	}

	@Override
	public BlockEntityWithoutLevelRenderer createRenderer() {
		return new ExtendoGripItemRenderer();
	}

}
