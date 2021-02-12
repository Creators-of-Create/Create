package com.simibubi.create.foundation.metadoc.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.metadoc.MetaDocElement;
import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.MetaDocScreen;

public abstract class MetaDocOverlayElement extends MetaDocElement {

	public void tick() {}

	public abstract void render(MetaDocScene scene, MetaDocScreen screen, MatrixStack ms, float partialTicks);

}
