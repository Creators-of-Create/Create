package com.simibubi.create.foundation.item;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IItemWithColorHandler {

	@OnlyIn(value = Dist.CLIENT)
	public IItemColor getColorHandler();
	
}
