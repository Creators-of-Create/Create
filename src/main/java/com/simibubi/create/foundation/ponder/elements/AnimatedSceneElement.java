package com.simibubi.create.foundation.ponder.elements;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public abstract class AnimatedSceneElement extends PonderSceneElement {

	protected Vector3d fadeVec;
	protected LerpedFloat fade;

	public AnimatedSceneElement() {
		fade = LerpedFloat.linear()
			.startWithValue(0);
	}

	public void forceApplyFade(float fade) {
		this.fade.startWithValue(fade);
	}

	public void setFade(float fade) {
		this.fade.setValue(fade);
	}

	public void setFadeVec(Vector3d fadeVec) {
		this.fadeVec = fadeVec;
	}

	@Override
	public final void renderFirst(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float pt) {
		ms.pushPose();
		float currentFade = applyFade(ms, pt);
		renderFirst(world, buffer, ms, currentFade, pt);
		ms.popPose();
	}

	@Override
	public final void renderLayer(PonderWorld world, IRenderTypeBuffer buffer, RenderType type, MatrixStack ms,
		float pt) {
		ms.pushPose();
		float currentFade = applyFade(ms, pt);
		renderLayer(world, buffer, type, ms, currentFade, pt);
		ms.popPose();
	}

	@Override
	public final void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float pt) {
		ms.pushPose();
		float currentFade = applyFade(ms, pt);
		renderLast(world, buffer, ms, currentFade, pt);
		ms.popPose();
	}

	protected float applyFade(MatrixStack ms, float pt) {
		float currentFade = fade.getValue(pt);
		if (fadeVec != null)
			MatrixTransformStack.of(ms)
				.translate(fadeVec.scale(-1 + currentFade));
		return currentFade;
	}

	protected void renderLayer(PonderWorld world, IRenderTypeBuffer buffer, RenderType type, MatrixStack ms, float fade,
		float pt) {}

	protected void renderFirst(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {}

	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {}

	protected int lightCoordsFromFade(float fade) {
		int light = 0xF000F0;
		if (fade != 1) {
			light = (int) (MathHelper.lerp(fade, 5, 0xF));
			light = light << 4 | light << 20;
		}
		return light;
	}

}
