package com.simibubi.create.content.equipment.tool;

import com.simibubi.create.AllTags.AllItemTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public class AllToolMaterials {
	public static final ToolMaterial CARDBOARD =
		new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0, 1, 2, 1, AllItemTags.CARDBOARD_PLATES.tag);

	private AllToolMaterials() {
	}
}
