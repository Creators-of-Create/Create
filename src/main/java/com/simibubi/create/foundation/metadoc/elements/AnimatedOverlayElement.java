package com.simibubi.create.foundation.metadoc.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.MetaDocScreen;
import com.simibubi.create.foundation.utility.LerpedFloat;

public abstract class AnimatedOverlayElement extends MetaDocOverlayElement {

	protected LerpedFloat fade;
	
	public AnimatedOverlayElement() {
		fade = LerpedFloat.linear()
			.startWithValue(0);
	}

	public void setFade(float fade) {
		this.fade.setValue(fade);
	}
	
	@Override
	public final void render(MetaDocScene scene, MetaDocScreen screen, MatrixStack ms, float partialTicks) {
		float currentFade = fade.getValue(partialTicks);
		render(scene, screen, ms, partialTicks, currentFade);
	}

	protected abstract void render(MetaDocScene scene, MetaDocScreen screen, MatrixStack ms, float partialTicks, float fade);

}
