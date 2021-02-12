package com.simibubi.create.foundation.metadoc.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.metadoc.MetaDocWorld;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.Vec3d;

public abstract class AnimatedSceneElement extends MetaDocSceneElement {

	protected Vec3d fadeVec;
	protected LerpedFloat fade;

	public AnimatedSceneElement() {
		fade = LerpedFloat.linear()
			.startWithValue(0);
	}

	public void setFade(float fade) {
		this.fade.setValue(fade);
	}

	public void setFadeVec(Vec3d fadeVec) {
		this.fadeVec = fadeVec;
	}

	@Override
	public final void render(MetaDocWorld world, IRenderTypeBuffer buffer, MatrixStack ms) {
		ms.push();
		float currentFade = fade.getValue(Minecraft.getInstance()
			.getRenderPartialTicks());
		if (fadeVec != null)
			MatrixStacker.of(ms)
				.translate(fadeVec.scale(-1 + currentFade));
		render(world, buffer, ms, currentFade);
		ms.pop();
	}

	protected abstract void render(MetaDocWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade);

	protected int lightCoordsFromFade(float fade) {
		int light = 0xF000F0;
		if (fade != 1) {
			light = (int) (0xF * fade);
			light = light << 4 | light << 20;
		}
		return light;
	}

}
