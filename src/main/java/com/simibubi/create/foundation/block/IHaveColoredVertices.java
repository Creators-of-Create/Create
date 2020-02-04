package com.simibubi.create.foundation.block;

import com.simibubi.create.foundation.block.render.ColoredVertexModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHaveColoredVertices extends IHaveCustomBlockModel {

	public int getColor(float x, float y, float z);

	@Override
	@OnlyIn(Dist.CLIENT)
	default IBakedModel createModel(IBakedModel original) {
		return new ColoredVertexModel(original, this);
	}

}
