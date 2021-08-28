package com.simibubi.create.content.curiosities.symmetry.client;

import com.simibubi.create.foundation.item.render.CreateCustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class SymmetryWandModel extends CreateCustomRenderedItemModel {

	public SymmetryWandModel(IBakedModel template) {
		super(template, "wand_of_symmetry");
		addPartials("bits", "core", "core_glow");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new SymmetryWandItemRenderer();
	}

}
