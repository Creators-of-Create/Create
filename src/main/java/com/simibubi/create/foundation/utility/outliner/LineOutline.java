package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class LineOutline extends Outline {

	protected Vec3d start = Vec3d.ZERO;
	protected Vec3d end = Vec3d.ZERO;

	public LineOutline set(Vec3d start, Vec3d end) {
		this.start = start;
		this.end = end;
		return this;
	}

	@Override
	public void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		renderCuboidLine(ms, buffer, start, end);
	}

	public static class EndChasingLineOutline extends LineOutline {

		float prevProgress = 0;
		float progress = 0;

		@Override
		public void tick() {
		}

		public EndChasingLineOutline setProgress(float progress) {
			prevProgress = this.progress;
			this.progress = progress;
			return this;
		}

		@Override
		public LineOutline set(Vec3d start, Vec3d end) {
			if (!end.equals(this.end))
				super.set(start, end);
			return this;
		}

		@Override
		public void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
			float pt = AnimationTickHolder.getPartialTicks();
			float distanceToTarget = 1 - MathHelper.lerp(pt, prevProgress, progress);
			Vec3d start = end.add(this.start.subtract(end)
				.scale(distanceToTarget));
			renderCuboidLine(ms, buffer, start, end);
		}

	}

}
