package com.simibubi.create.content.logistics.trains.track;

import static com.simibubi.create.AllBlockPartials.GIRDER_SEGMENT;
import static com.simibubi.create.AllBlockPartials.GIRDER_SEGMENT_2;
import static com.simibubi.create.AllBlockPartials.TRACK_SEGMENT_LEFT;
import static com.simibubi.create.AllBlockPartials.TRACK_SEGMENT_RIGHT;
import static com.simibubi.create.AllBlockPartials.TRACK_TIE;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.repack.joml.Math;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.content.logistics.trains.BezierConnection.GirderAngles;
import com.simibubi.create.content.logistics.trains.BezierConnection.SegmentAngles;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackRenderer extends SafeTileEntityRenderer<TrackTileEntity> {

	public TrackRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(TrackTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		if (Backend.isOn())
			return;

		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
		te.connections.forEach(map -> map.values()
			.forEach(bc -> renderBezierTurn(bc, ms, vb)));
	}

	public static void renderBezierTurn(BezierConnection bc, PoseStack ms, VertexConsumer vb) {
		if (!bc.isPrimary())
			return;

		ms.pushPose();
		BlockPos tePosition = bc.tePositions.getFirst();
		BlockState air = Blocks.AIR.defaultBlockState();
		ClientLevel level = Minecraft.getInstance().level;
		SegmentAngles[] segments = bc.getBakedSegments();

		TransformStack.cast(ms)
			.nudge((int) tePosition.asLong());

		renderGirder(bc, ms, vb, tePosition);

		for (int i = 1; i < segments.length; i++) {
			SegmentAngles segment = segments[i];
			int light = LevelRenderer.getLightColor(level, segment.lightPosition.offset(tePosition));

			CachedBufferer.partial(TRACK_TIE, air)
				.mulPose(segment.tieTransform)
				.disableDiffuseMult()
				.light(light)
				.renderInto(ms, vb);

			for (boolean first : Iterate.trueAndFalse)
				CachedBufferer.partial(first ? TRACK_SEGMENT_LEFT : TRACK_SEGMENT_RIGHT, air)
					.mulPose(segment.railTransforms.get(first))
					.disableDiffuseMult()
					.light(light)
					.renderInto(ms, vb);
		}

		ms.popPose();
	}

	private static void renderGirder(BezierConnection bc, PoseStack ms, VertexConsumer vb, BlockPos tePosition) {
		if (!bc.hasGirder)
			return;

		BlockState air = Blocks.AIR.defaultBlockState();
		ClientLevel level = Minecraft.getInstance().level;
		GirderAngles[] girders = bc.getBakedGirders();

		for (int i = 1; i < girders.length; i++) {
			GirderAngles segment = girders[i];
			int light = LevelRenderer.getLightColor(level, segment.lightPosition.offset(tePosition));

			for (boolean first : Iterate.trueAndFalse) {
				CachedBufferer.partial(GIRDER_SEGMENT_2, air)
					.mulPose(segment.beams.get(first))
					.disableDiffuseMult()
					.light(light)
					.renderInto(ms, vb);

				for (boolean top : Iterate.trueAndFalse)
					CachedBufferer.partial(GIRDER_SEGMENT, air)
						.mulPose(segment.beamCaps.get(top)
							.get(first))
						.disableDiffuseMult()
						.light(light)
						.renderInto(ms, vb);
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
	public boolean shouldRenderOffScreen(TrackTileEntity pBlockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96 * 2;
	}

}
