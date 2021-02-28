package com.simibubi.create.foundation.ponder.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.ponder.PonderElement;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

public abstract class PonderSceneElement extends PonderElement {

	public abstract void renderFirst(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float pt);
	
	public abstract void renderLayer(PonderWorld world, IRenderTypeBuffer buffer, RenderType type, MatrixStack ms, float pt);
	
	public abstract void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float pt);
	
}
