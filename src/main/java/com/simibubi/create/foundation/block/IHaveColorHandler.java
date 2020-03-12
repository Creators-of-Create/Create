package com.simibubi.create.foundation.block;

import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHaveColorHandler {

	@OnlyIn(Dist.CLIENT)
	public IBlockColor getColorHandler();
	
}
