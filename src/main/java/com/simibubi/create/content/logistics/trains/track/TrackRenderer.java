package com.simibubi.create.content.logistics.trains.track;

import static com.simibubi.create.AllPartialModels.GIRDER_SEGMENT_BOTTOM;
import static com.simibubi.create.AllPartialModels.GIRDER_SEGMENT_MIDDLE;
import static com.simibubi.create.AllPartialModels.GIRDER_SEGMENT_TOP;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.content.logistics.trains.BezierConnection.GirderAngles;
import com.simibubi.create.content.logistics.trains.BezierConnection.SegmentAngles;
import com.simibubi.create.content.logistics.trains.TrackMaterial;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackRenderer extends SafeBlockEntityRenderer<TrackBlockEntity> {

	public TrackRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(TrackBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		Level level = be.getLevel();
		if (Backend.canUseInstancing(level))
			return;
		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
		be.connections.values()
			.forEach(bc -> renderBezierTurn(level, bc, ms, vb));
	}

	public static void renderBezierTurn(Level level, BezierConnection bc, PoseStack ms, VertexConsumer vb) {
		if (!bc.isPrimary())
			return;

		ms.pushPose();
		BlockPos tePosition = bc.tePositions.getFirst();
		BlockState air = Blocks.AIR.defaultBlockState();
		SegmentAngles[] segments = bc.getBakedSegments();

		renderGirder(level, bc, ms, vb, tePosition);

		for (int i = 1; i < segments.length; i++) {
			SegmentAngles segment = segments[i];
			int light = LevelRenderer.getLightColor(level, segment.lightPosition.offset(tePosition));

			TrackMaterial.TrackModelHolder modelHolder = bc.getMaterial().getModelHolder();

			CachedBufferer.partial(modelHolder.tie(), air)
				.mulPose(segment.tieTransform.pose())
				.mulNormal(segment.tieTransform.normal())
				.light(light)
				.renderInto(ms, vb);

			for (boolean first : Iterate.trueAndFalse) {
				Pose transform = segment.railTransforms.get(first);
				CachedBufferer.partial(first ? modelHolder.segment_left() : modelHolder.segment_right(), air)
					.mulPose(transform.pose())
					.mulNormal(transform.normal())
					.light(light)
					.renderInto(ms, vb);
			}
		}

		ms.popPose();
	}

	private static void renderGirder(Level level, BezierConnection bc, PoseStack ms, VertexConsumer vb,
		BlockPos tePosition) {
		if (!bc.hasGirder)
			return;

		BlockState air = Blocks.AIR.defaultBlockState();
		GirderAngles[] girders = bc.getBakedGirders();

		for (int i = 1; i < girders.length; i++) {
			GirderAngles segment = girders[i];
			int light = LevelRenderer.getLightColor(level, segment.lightPosition.offset(tePosition));

			for (boolean first : Iterate.trueAndFalse) {
				Pose beamTransform = segment.beams.get(first);
				CachedBufferer.partial(GIRDER_SEGMENT_MIDDLE, air)
					.mulPose(beamTransform.pose())
					.mulNormal(beamTransform.normal())
					.light(light)
					.renderInto(ms, vb);

				for (boolean top : Iterate.trueAndFalse) {
					Pose beamCapTransform = segment.beamCaps.get(top)
						.get(first);
					CachedBufferer.partial(top ? GIRDER_SEGMENT_TOP : GIRDER_SEGMENT_BOTTOM, air)
						.mulPose(beamCapTransform.pose())
						.mulNormal(beamCapTransform.normal())
						.light(light)
						.renderInto(ms, vb);
				}
			}
		}
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
	public boolean shouldRenderOffScreen(TrackBlockEntity pBlockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96 * 2;
	}

}
