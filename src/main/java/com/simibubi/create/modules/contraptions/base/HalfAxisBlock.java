package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.foundation.block.IRenderUtilityBlock;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.block.material.Material;

public class HalfAxisBlock extends ProperDirectionalBlock implements IRenderUtilityBlock {

	public HalfAxisBlock() {
		super(Properties.create(Material.ROCK));
	}

}
