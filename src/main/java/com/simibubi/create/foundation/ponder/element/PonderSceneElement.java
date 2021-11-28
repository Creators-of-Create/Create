package com.simibubi.create.foundation.ponder.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public abstract class PonderSceneElement extends PonderElement {

	public abstract void renderFirst(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float pt);
	
	public abstract void renderLayer(PonderWorld world, MultiBufferSource buffer, RenderType type, PoseStack ms, float pt);
	
	public abstract void renderLast(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float pt);
	
}
