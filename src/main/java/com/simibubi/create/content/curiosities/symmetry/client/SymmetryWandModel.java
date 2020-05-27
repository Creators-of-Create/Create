package com.simibubi.create.content.curiosities.symmetry.client;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class SymmetryWandModel extends CustomRenderedItemModel {

	public SymmetryWandModel(IBakedModel template) {
		super(template, "wand_of_symmetry");
		addPartials("bits", "core");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new SymmetryWandItemRenderer();
	}
	
}
