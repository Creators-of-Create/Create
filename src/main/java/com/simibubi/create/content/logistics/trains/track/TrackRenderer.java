package com.simibubi.create.content.logistics.trains.track;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.repack.joml.Math;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class TrackRenderer extends SafeTileEntityRenderer<TrackTileEntity> {

	public TrackRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(TrackTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		if (Backend.isOn()) return;

		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		te.connections.forEach(map -> map.values()
			.forEach(bc -> renderBezierTurn(bc, ms, vb)));
	}

	public static void renderBezierTurn(BezierConnection bc, PoseStack ms, VertexConsumer vb) {
		if (!bc.isPrimary())
			return;

		ms.pushPose();
		BlockPos tePosition = bc.tePositions.getFirst();

		TransformStack.cast(ms)
				.nudge((int) tePosition.asLong());

		Vec3 previous1 = null;
		Vec3 previous2 = null;

		for (BezierConnection.Segment segment : bc) {
			Vec3 rail1 = segment.position.add(segment.normal.scale(.965f));
			Vec3 rail2 = segment.position.subtract(segment.normal.scale(.965f));

			if (previous1 != null) {
				{
					// Tie
					Vec3 railMiddle = rail1.add(rail2)
						.scale(.5);
					Vec3 prevMiddle = previous1.add(previous2)
						.scale(.5);
					Vec3 diff = railMiddle.subtract(prevMiddle);
					Vec3 angles = getModelAngles(segment.normal, diff);

					SuperByteBuffer sbb =
						CachedBufferer.partial(AllBlockPartials.TRACK_TIE, Blocks.AIR.defaultBlockState());

					sbb.translate(prevMiddle)
						.rotateYRadians(angles.y)
						.rotateXRadians(angles.x)
						.rotateZRadians(angles.z)
						.translate(-1 / 2f, -2 / 16f - 1 / 1024f, 0);

					sbb.light(LevelRenderer.getLightColor(Minecraft.getInstance().level,
						new BlockPos(railMiddle).offset(tePosition)));
					sbb.renderInto(ms, vb);
				}

				// Rails
				for (boolean first : Iterate.trueAndFalse) {
					Vec3 railI = first ? rail1 : rail2;
					Vec3 prevI = first ? previous1 : previous2;
					Vec3 diff = railI.subtract(prevI);
					Vec3 angles = getModelAngles(segment.normal, diff);

					SuperByteBuffer sbb = CachedBufferer.partial(
						first ? AllBlockPartials.TRACK_SEGMENT_LEFT : AllBlockPartials.TRACK_SEGMENT_RIGHT,
						Blocks.AIR.defaultBlockState());

					sbb.translate(prevI)
						.rotateYRadians(angles.y)
						.rotateXRadians(angles.x)
						.rotateZRadians(angles.z)
						.translate(0, -2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, 0)
						.scale(1, 1, (float) diff.length() * 2.1f);

					sbb.light(LevelRenderer.getLightColor(Minecraft.getInstance().level,
						new BlockPos(prevI).offset(tePosition)));
					sbb.renderInto(ms, vb);
				}
			}

			previous1 = rail1;
			previous2 = rail2;
		}

		ms.popPose();
	}

	public static Vec3 getModelAngles(Vec3 normal, Vec3 diff) {
		double diffX = diff.x();
		double diffY = diff.y();
		double diffZ = diff.z();
		double len = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));
		double yaw = Mth.atan2(diffX, diffZ);
		double pitch = Mth.atan2(len, diffY) - Math.PI * .5;

		Vec3 yawPitchNormal = VecHelper.rotate(VecHelper.rotate(new Vec3(0, 1, 0), AngleHelper.deg(pitch), Axis.X),
			AngleHelper.deg(yaw), Axis.Y);

		double signum = Math.signum(yawPitchNormal.dot(normal));
		if (Math.abs(signum) < 0.5f)
			signum = yawPitchNormal.distanceToSqr(normal) < 0.5f ? -1 : 1;
		double dot = diff.cross(normal)
			.normalize()
			.dot(yawPitchNormal);
		double roll = Math.acos(Mth.clamp(dot, -1, 1)) * signum;
		return new Vec3(pitch, yaw, roll);
	}

	@Override
	public boolean shouldRenderOffScreen(TrackTileEntity pBlockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96 * 2;
	}

}
