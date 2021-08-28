package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class WorldshaperModel extends CreateCustomRenderedItemModel {

	public WorldshaperModel(IBakedModel template) {
		super(template, "handheld_worldshaper");
		addPartials("core", "core_glow", "accelerator");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new WorldshaperItemRenderer();
	}

}
