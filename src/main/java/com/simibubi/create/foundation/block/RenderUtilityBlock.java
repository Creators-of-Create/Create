package com.simibubi.create.foundation.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class RenderUtilityBlock extends Block implements IRenderUtilityBlock {

	public RenderUtilityBlock() {
		super(Properties.create(Material.AIR).noDrops());
	}

}
