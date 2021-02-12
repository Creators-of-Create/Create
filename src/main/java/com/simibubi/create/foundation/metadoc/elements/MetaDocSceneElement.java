package com.simibubi.create.foundation.metadoc.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.metadoc.MetaDocElement;
import com.simibubi.create.foundation.metadoc.MetaDocWorld;

import net.minecraft.client.renderer.IRenderTypeBuffer;

public abstract class MetaDocSceneElement extends MetaDocElement {

	public abstract void render(MetaDocWorld world, IRenderTypeBuffer buffer, MatrixStack ms);
	
}
