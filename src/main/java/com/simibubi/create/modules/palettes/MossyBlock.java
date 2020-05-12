package com.simibubi.create.modules.palettes;

import com.simibubi.create.foundation.block.IHaveColorHandler;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MossyBlock extends Block implements IHaveColorHandler {

	public MossyBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IBlockColor getColorHandler() {
		return new StandardFoliageColorHandler();
	}

}
