package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class WorldshaperModel extends CustomRenderedItemModel {

	public WorldshaperModel(IBakedModel template) {
		super(template, "handheld_worldshaper");
		addPartials("core", "core_glow", "accelerator");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new WorldshaperItemRenderer();
	}

}
