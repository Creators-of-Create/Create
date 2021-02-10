package com.simibubi.create.foundation.metadoc;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;

public abstract class MetaDocSceneElement {

	boolean visible = true;

	public abstract void render(MetaDocWorld world, IRenderTypeBuffer buffer, MatrixStack ms);

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
}
