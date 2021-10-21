package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;

public class ExtendoGripModel extends CreateCustomRenderedItemModel {

	public ExtendoGripModel(IBakedModel template) {
		super(template, "extendo_grip");
		addPartials("cog", "thin_short", "wide_short", "thin_long", "wide_long");
	}

}
