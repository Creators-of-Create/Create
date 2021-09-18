package com.simibubi.create.content.curiosities.weapons;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

public class PotatoCannonModel extends CreateCustomRenderedItemModel {

	public PotatoCannonModel(BakedModel template) {
		super(template, "potato_cannon");
		addPartials("cog");
	}

	@Override
	public BlockEntityWithoutLevelRenderer createRenderer() {
		Minecraft minecraft = Minecraft.getInstance();
		return new PotatoCannonItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
	}

}
