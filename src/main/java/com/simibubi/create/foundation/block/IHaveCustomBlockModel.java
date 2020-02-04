package com.simibubi.create.foundation.block;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHaveCustomBlockModel {
	
	@OnlyIn(value = Dist.CLIENT)
	public IBakedModel createModel(@Nullable IBakedModel original);

}
