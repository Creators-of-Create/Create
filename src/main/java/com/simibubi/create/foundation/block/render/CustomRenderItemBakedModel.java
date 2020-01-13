package com.simibubi.create.foundation.block.render;

import com.simibubi.create.Create;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;

public abstract class CustomRenderItemBakedModel extends WrappedBakedModel {

	public CustomRenderItemBakedModel(IBakedModel template) {
		super(template);
	}
	
	public abstract CustomRenderItemBakedModel loadPartials(ModelBakeEvent event); 

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}
	
	protected static IBakedModel loadCustomModel(ModelBakeEvent event, String name) {
		return event.getModelLoader().func_217845_a(new ResourceLocation(Create.ID, "item/" + name),
				ModelRotation.X0_Y0);
	}

}
