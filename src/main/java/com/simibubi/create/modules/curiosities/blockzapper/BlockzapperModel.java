package com.simibubi.create.modules.curiosities.blockzapper;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.curiosities.blockzapper.BlockzapperItem.ComponentTier;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

public class BlockzapperModel extends CustomRenderedItemModel {

	public BlockzapperModel(IBakedModel template) {
		super(template, "blockzapper");
		addPartials("core", "body", "amplifier_core", "accelerator", "gold_body", "gold_scope", "gold_amplifier",
				"gold_retriever", "gold_accelerator", "chorus_body", "chorus_amplifier", "chorus_retriever",
				"chorus_accelerator");
	}

	@Override
	public ItemStackTileEntityRenderer createRenderer() {
		return new BlockzapperItemRenderer();
	}

	@Nullable
	IBakedModel getComponentPartial(BlockzapperItem.ComponentTier tier, BlockzapperItem.Components component) {
		String prefix = tier == ComponentTier.Chromatic ? "chorus_" : tier == ComponentTier.Brass ? "gold_" : "";
		return getPartial(prefix + Lang.asId(component.name()));
	}

}
