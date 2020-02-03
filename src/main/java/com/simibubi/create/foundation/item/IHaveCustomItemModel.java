package com.simibubi.create.foundation.item;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHaveCustomItemModel {

	@OnlyIn(value = Dist.CLIENT)
	public CustomRenderedItemModel createModel(@Nullable IBakedModel original);

}
