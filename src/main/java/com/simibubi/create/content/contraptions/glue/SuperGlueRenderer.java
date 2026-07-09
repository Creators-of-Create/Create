package com.simibubi.create.content.contraptions.glue;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.neoforged.api.distmarker.Dist;

public class SuperGlueRenderer extends EntityRenderer<SuperGlueEntity, EntityRenderState> {

	public SuperGlueRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}

	@Override
	public boolean shouldRender(SuperGlueEntity entity, Frustum frustum, double x, double y, double z) {
		return false;
	}

}
