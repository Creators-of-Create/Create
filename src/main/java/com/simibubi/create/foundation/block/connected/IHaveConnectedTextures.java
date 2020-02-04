package com.simibubi.create.foundation.block.connected;

import com.simibubi.create.foundation.block.IHaveCustomBlockModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHaveConnectedTextures extends IHaveCustomBlockModel {

	public ConnectedTextureBehaviour getBehaviour();

	@Override
	@OnlyIn(Dist.CLIENT)
	default IBakedModel createModel(IBakedModel original) {
		return new CTModel(original, this);
	}
	
}
