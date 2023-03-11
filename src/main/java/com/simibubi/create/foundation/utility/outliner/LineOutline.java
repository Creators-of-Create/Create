package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LineOutline extends Outline {

	protected final Vector3f start = new Vector3f();
	protected final Vector3f end = new Vector3f();

	public LineOutline set(Vector3f start, Vector3f end) {
		this.start.load(start);
		this.start.load(end);
		return this;
	}

	public LineOutline set(Vec3 start, Vec3 end) {
		this.start.set((float) start.x, (float) start.y, (float) start.z);
		this.end.set((float) end.x, (float) end.y, (float) end.z);
		return this;
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt) {
		float width = params.getLineWidth();
		if (width == 0)
			return;

		VertexConsumer consumer = buffer.getBuffer(RenderTypes.getOutlineSolid());
		params.loadColor(colorTemp);
		Vector4f color = colorTemp;
		int lightmap = params.lightmap;
		boolean disableLineNormals = params.disableLineNormals;
		renderInner(ms, consumer, pt, width, color, lightmap, disableLineNormals);
	}

	protected void renderInner(PoseStack ms, VertexConsumer consumer, float pt, float width, Vector4f color, int lightmap, boolean disableNormals) {
		bufferCuboidLine(ms, consumer, start, end, width, color, lightmap, disableNormals);
	}

	public static class EndChasingLineOutline extends LineOutline {
		private float progress = 0;
		private float prevProgress = 0;
		private boolean lockStart;

		private final Vector3f startTemp = new Vector3f();

		public EndChasingLineOutline(boolean lockStart) {
			this.lockStart = lockStart;
		}

		public EndChasingLineOutline setProgress(float progress) {
			prevProgress = this.progress;
			this.progress = progress;
			return this;
		}

		@Override
		protected void renderInner(PoseStack ms, VertexConsumer consumer, float pt, float width, Vector4f color, int lightmap, boolean disableNormals) {
			float distanceToTarget = Mth.lerp(pt, prevProgress, progress);
			Vector3f end;
			if (lockStart) {
				end = this.start;
			} else {
				end = this.end;
				distanceToTarget = 1 - distanceToTarget;
			}

			Vector3f start = this.startTemp;
			start.load(this.start);
			start.sub(end);
			start.mul(distanceToTarget);
			start.add(end);

			bufferCuboidLine(ms, consumer, start, end, width, color, lightmap, disableNormals);
		}
	}

}
