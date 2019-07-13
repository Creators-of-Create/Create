package com.simibubi.create.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class RenderingBlock extends Block implements IJustForRendering {

	public RenderingBlock() {
		super(Properties.create(Material.AIR));
	}

}
