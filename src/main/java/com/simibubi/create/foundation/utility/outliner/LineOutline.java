package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LineOutline extends Outline {

	protected Vector3d start = Vector3d.ZERO;
	protected Vector3d end = Vector3d.ZERO;

	public LineOutline set(Vector3d start, Vector3d end) {
		this.start = start;
		this.end = end;
		return this;
	}

	@Override
	public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, float pt) {
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
		public LineOutline set(Vector3d start, Vector3d end) {
			if (!end.equals(this.end))
				super.set(start, end);
			return this;
		}

		@Override
		public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, float pt) {
			float distanceToTarget = 1 - MathHelper.lerp(pt, prevProgress, progress);
			Vector3d start = end.add(this.start.subtract(end)
				.scale(distanceToTarget));
			renderCuboidLine(ms, buffer, start, end);
		}

	}

}
