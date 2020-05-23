package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class TerrainzapperModel extends CustomRenderedItemModel {

	public TerrainzapperModel(IBakedModel template) {
		super(template, "blockzapper");
		addPartials("terrain_core", "terrain_accelerator");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new TerrainzapperItemRenderer();
	}

}
