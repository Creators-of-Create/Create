package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LineOutline extends Outline {

	protected Vec3 start = Vec3.ZERO;
	protected Vec3 end = Vec3.ZERO;

	public LineOutline set(Vec3 start, Vec3 end) {
		this.start = start;
		this.end = end;
		return this;
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt) {
		renderCuboidLine(ms, buffer, start, end);
	}

	public static class EndChasingLineOutline extends LineOutline {

		float prevProgress = 0;
		float progress = 0;
		private boolean lockStart;

		public EndChasingLineOutline(boolean lockStart) {
			this.lockStart = lockStart;
		}

		@Override
		public void tick() {}

		public EndChasingLineOutline setProgress(float progress) {
			prevProgress = this.progress;
			this.progress = progress;
			return this;
		}

		@Override
		public LineOutline set(Vec3 start, Vec3 end) {
			if (!end.equals(this.end))
				super.set(start, end);
			return this;
		}

		@Override
		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt) {
			float distanceToTarget = Mth.lerp(pt, prevProgress, progress);
			if (!lockStart)
				distanceToTarget = 1 - distanceToTarget;
			Vec3 start = lockStart ? this.end : this.start;
			Vec3 end = lockStart ? this.start : this.end;
			
			start = end.add(this.start.subtract(end)
				.scale(distanceToTarget));
			renderCuboidLine(ms, buffer, start, end);
		}

	}

}
