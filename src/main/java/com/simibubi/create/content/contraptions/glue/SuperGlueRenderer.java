package com.simibubi.create.content.contraptions.glue;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuperGlueRenderer extends EntityRenderer<SuperGlueEntity> {

	public SuperGlueRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(SuperGlueEntity entity) {
		return null;
	}

	@Override
	public boolean shouldRender(SuperGlueEntity entity, Frustum frustum, double x, double y, double z) {
		return false;
	}

}
