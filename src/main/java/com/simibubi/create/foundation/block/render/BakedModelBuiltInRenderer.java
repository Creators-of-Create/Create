package com.simibubi.create.foundation.block.render;

import net.minecraft.client.renderer.model.IBakedModel;

public class BakedModelBuiltInRenderer extends WrappedBakedModel {

	public BakedModelBuiltInRenderer(IBakedModel template) {
		super(template);
	}
	
	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

}
