package com.simibubi.create.foundation.block;

import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.Material;

public class RenderUtilityAxisBlock extends RotatedPillarBlock implements IRenderUtilityBlock {

	public RenderUtilityAxisBlock() {
		super(Properties.create(Material.AIR));

	}
}
