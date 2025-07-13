package com.simibubi.create.content.trains.track;

import static com.simibubi.create.AllPartialModels.GIRDER_SEGMENT_BOTTOM;
import static com.simibubi.create.AllPartialModels.GIRDER_SEGMENT_MIDDLE;
import static com.simibubi.create.AllPartialModels.GIRDER_SEGMENT_TOP;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.track.BezierConnection.GirderAngles;
import com.simibubi.create.content.trains.track.BezierConnection.SegmentAngles;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
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
		if (VisualizationManager.supportsVisualization(level))
			return;
		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
		be.connections.values()
			.forEach(bc -> renderBezierTurn(level, bc, ms, vb));
	}

	public static void renderBezierTurn(Level level, BezierConnection bc, PoseStack ms, VertexConsumer vb) {
		if (!bc.isPrimary())
			return;

		ms.pushPose();
		BlockPos bePosition = bc.bePositions.getFirst();
		BlockState air = Blocks.AIR.defaultBlockState();
		SegmentAngles segment = bc.getBakedSegments();

		renderGirder(level, bc, ms, vb, bePosition);

		for (int i = 1; i < segment.length; i++) {
			int light = LevelRenderer.getLightColor(level, segment.lightPosition[i].offset(bePosition));

			TrackMaterial.TrackModelHolder modelHolder = bc.getMaterial().getModelHolder();

			CachedBuffers.partial(modelHolder.tie(), air)
				.mulPose(segment.tieTransform[i].pose())
				.mulNormal(segment.tieTransform[i].normal())
				.light(light)
				.renderInto(ms, vb);

			for (boolean first : Iterate.trueAndFalse) {
				Pose transform = segment.railTransforms[i].get(first);
				CachedBuffers.partial(first ? modelHolder.leftSegment() : modelHolder.rightSegment(), air)
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
		GirderAngles segment = bc.getBakedGirders();

		for (int i = 1; i < segment.length; i++) {
			int light = LevelRenderer.getLightColor(level, segment.lightPosition[i].offset(tePosition));

			for (boolean first : Iterate.trueAndFalse) {
				Pose beamTransform = segment.beams[i].get(first);
				CachedBuffers.partial(GIRDER_SEGMENT_MIDDLE, air)
					.mulPose(beamTransform.pose())
					.mulNormal(beamTransform.normal())
					.light(light)
					.renderInto(ms, vb);

				for (boolean top : Iterate.trueAndFalse) {
					Pose beamCapTransform = segment.beamCaps[i].get(top)
						.get(first);
					CachedBuffers.partial(top ? GIRDER_SEGMENT_TOP : GIRDER_SEGMENT_BOTTOM, air)
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
