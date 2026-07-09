package com.simibubi.create.content.logistics;

import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.transform.Translate;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FlapStuffs {
	public static final int FLAP_COUNT = 4;
	public static final float X_OFFSET = 0.075f / 16f;
	public static final float SEGMENT_STEP = -3.05f / 16f;
	public static final Vec3 TUNNEL_PIVOT = VecHelper.voxelSpace(0, 10, 1f);
	public static final Vec3 FUNNEL_PIVOT = VecHelper.voxelSpace(0, 10, 9.5f);

	public static void renderFlaps(PoseStack ms, VertexConsumer vb, SuperByteBuffer flapBuffer, Vec3 pivot, Direction funnelFacing, float flapness, float zOffset, int light) {
		float horizontalAngle = AngleHelper.horizontalAngle(funnelFacing.getOpposite());

		var msr = TransformStack.of(ms);
		ms.pushPose();
		msr.center()
			.rotateYDegrees(horizontalAngle)
			.uncenter();
		ms.translate(X_OFFSET, 0, zOffset);

		for (int segment = 0; segment < FLAP_COUNT; segment++) {
			ms.pushPose();

			msr.translate(pivot)
				.rotateXDegrees(flapAngle(flapness, segment))
				.translateBack(pivot);

			flapBuffer.light(light)
				.renderInto(ms, vb);

			ms.popPose();
			ms.translate(SEGMENT_STEP, 0, 0);
		}
		ms.popPose();
	}

	public static float flapAngle(float flapness, int segment) {
		float intensity = segment == 3 ? 1.5f : segment + 1;
		float abs = Math.abs(flapness);
		float flapAngle = Mth.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * flapness;
		if (flapness < 0)
			flapAngle *= .5f;
		return flapAngle;
	}

	public static Matrix4f commonTransform(BlockPos visualPosition, Direction side, float baseZOffset) {
		float horizontalAngle = AngleHelper.horizontalAngle(side.getOpposite());

		return new Matrix4f()
			.translate(visualPosition.getX(), visualPosition.getY(), visualPosition.getZ())
			.translate(Translate.CENTER, Translate.CENTER, Translate.CENTER)
			.rotateY(Mth.DEG_TO_RAD * horizontalAngle)
			.translate(-Translate.CENTER, -Translate.CENTER, -Translate.CENTER)
			.translate(X_OFFSET, 0, baseZOffset);
	}

	public static class Visual {
		private final TransformedInstance[] flaps;

		private final Matrix4f commonTransform = new Matrix4f();
		private final Vec3 pivot;

		public Visual(InstancerProvider instancerProvider, Matrix4fc commonTransform, Vec3 pivot, Model flapModel) {
			this.pivot = pivot;
			this.commonTransform.set(commonTransform)
				.translate((float) pivot.x, (float) pivot.y, (float) pivot.z);

			flaps = new TransformedInstance[FLAP_COUNT];

			instancerProvider.instancer(InstanceTypes.TRANSFORMED, flapModel)
				.createInstances(flaps);
		}

		public void update(float f) {
			for (int segment = 0; segment < FLAP_COUNT; segment++) {
				var flap = flaps[segment];

				flap.setTransform(commonTransform)
					.rotateXDegrees(flapAngle(f, segment))
					.translateBack(pivot)
					.translate(segment * SEGMENT_STEP, 0, 0)
					.setChanged();
			}
		}

		public void delete() {
			for (TransformedInstance flap : flaps) {
				flap.delete();
			}
		}

		public void updateLight(int light) {
			for (TransformedInstance flap : flaps) {
				flap.light(light)
					.setChanged();
			}
		}

		public void collectCrumblingInstances(Consumer<Instance> consumer) {
			for (TransformedInstance flap : flaps) {
				consumer.accept(flap);
			}
		}
	}
}
