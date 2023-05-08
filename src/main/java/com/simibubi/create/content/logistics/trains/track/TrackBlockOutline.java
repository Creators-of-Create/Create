package com.simibubi.create.content.logistics.trains.track;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class TrackBlockOutline {

	public static WorldAttached<Map<BlockPos, TrackTileEntity>> TRACKS_WITH_TURNS =
		new WorldAttached<>(w -> new HashMap<>());

	public static BezierPointSelection result;

	public static void pickCurves() {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.cameraEntity instanceof LocalPlayer player))
			return;
		if (mc.level == null)
			return;

		Vec3 origin = player.getEyePosition(AnimationTickHolder.getPartialTicks(mc.level));

		double maxRange = mc.hitResult == null ? Double.MAX_VALUE
			: mc.hitResult.getLocation()
				.distanceToSqr(origin);

		result = null;

		AttributeInstance range = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
		Vec3 target = RaycastHelper.getTraceTarget(player, Math.min(maxRange, range.getValue()) + 1, origin);
		Map<BlockPos, TrackTileEntity> turns = TRACKS_WITH_TURNS.get(mc.level);

		for (TrackTileEntity te : turns.values()) {
			for (BezierConnection bc : te.connections.values()) {
				if (!bc.isPrimary())
					continue;

				AABB bounds = bc.getBounds();
				if (!bounds.contains(origin) && bounds.clip(origin, target)
					.isEmpty())
					continue;

				float[] stepLUT = bc.getStepLUT();
				int segments = (int) (bc.getLength() * 2);
				AABB segmentBounds = AllShapes.TRACK_ORTHO.get(Direction.SOUTH)
					.bounds();
				segmentBounds = segmentBounds.move(-.5, segmentBounds.getYsize() / -2, -.5);

				int bestSegment = -1;
				double bestDistance = Double.MAX_VALUE;
				double newMaxRange = maxRange;

				for (int i = 0; i < stepLUT.length - 2; i++) {
					float t = stepLUT[i] * i / segments;
					float t1 = stepLUT[i + 1] * (i + 1) / segments;
					float t2 = stepLUT[i + 2] * (i + 2) / segments;

					Vec3 v1 = bc.getPosition(t);
					Vec3 v2 = bc.getPosition(t2);
					Vec3 diff = v2.subtract(v1);
					Vec3 angles = TrackRenderer.getModelAngles(bc.getNormal(t1), diff);

					Vec3 anchor = v1.add(diff.scale(.5));
					Vec3 localOrigin = origin.subtract(anchor);
					Vec3 localDirection = target.subtract(origin);
					localOrigin = VecHelper.rotate(localOrigin, AngleHelper.deg(-angles.x), Axis.X);
					localOrigin = VecHelper.rotate(localOrigin, AngleHelper.deg(-angles.y), Axis.Y);
					localDirection = VecHelper.rotate(localDirection, AngleHelper.deg(-angles.x), Axis.X);
					localDirection = VecHelper.rotate(localDirection, AngleHelper.deg(-angles.y), Axis.Y);

					Optional<Vec3> clip = segmentBounds.clip(localOrigin, localOrigin.add(localDirection));
					if (clip.isEmpty())
						continue;

					if (bestSegment != -1 && bestDistance < clip.get()
						.distanceToSqr(0, 0.25f, 0))
						continue;

					double distanceToSqr = clip.get()
						.distanceToSqr(localOrigin);
					if (distanceToSqr > maxRange)
						continue;

					bestSegment = i;
					newMaxRange = distanceToSqr;
					bestDistance = clip.get()
						.distanceToSqr(0, 0.25f, 0);

					BezierTrackPointLocation location = new BezierTrackPointLocation(bc.getKey(), i);
					result = new BezierPointSelection(te, location, anchor, angles, diff.normalize());
				}

				if (bestSegment != -1)
					maxRange = newMaxRange;
			}
		}

		if (result == null)
			return;

		if (mc.hitResult != null && mc.hitResult.getType() != Type.MISS) {
			Vec3 priorLoc = mc.hitResult.getLocation();
			mc.hitResult = BlockHitResult.miss(priorLoc, Direction.UP, new BlockPos(priorLoc));
		}
	}

	public static void drawCurveSelection(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;

		BezierPointSelection result = TrackBlockOutline.result;
		if (result == null)
			return;

		VertexConsumer vb = buffer.getBuffer(RenderType.lines());
		Vec3 vec = result.vec()
			.subtract(camera);
		Vec3 angles = result.angles();
		TransformStack.cast(ms)
			.pushPose()
			.translate(vec.x, vec.y + .125f, vec.z)
			.rotateYRadians(angles.y)
			.rotateXRadians(angles.x)
			.translate(-.5, -.125f, -.5);

		boolean holdingTrack = AllBlocks.TRACK.isIn(Minecraft.getInstance().player.getMainHandItem());
		renderShape(AllShapes.TRACK_ORTHO.get(Direction.SOUTH), ms, vb, holdingTrack ? false : null);
		ms.popPose();
	}

	@SubscribeEvent
	public static void drawCustomBlockSelection(DrawSelectionEvent.HighlightBlock event) {
		Minecraft mc = Minecraft.getInstance();
		BlockHitResult target = event.getTarget();
		BlockPos pos = target.getBlockPos();
		BlockState blockstate = mc.level.getBlockState(pos);

		if (!(blockstate.getBlock() instanceof TrackBlock))
			return;
		if (!mc.level.getWorldBorder()
			.isWithinBounds(pos))
			return;

		VertexConsumer vb = event.getMultiBufferSource()
			.getBuffer(RenderType.lines());
		Vec3 camPos = event.getCamera()
			.getPosition();

		PoseStack ms = event.getPoseStack();

		ms.pushPose();
		ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);

		boolean holdingTrack = AllBlocks.TRACK.isIn(Minecraft.getInstance().player.getMainHandItem());
		TrackShape shape = blockstate.getValue(TrackBlock.SHAPE);
		boolean isJunction = shape.isJunction();
		walkShapes(shape, TransformStack.cast(ms), s -> {
			renderShape(s, ms, vb, holdingTrack ? !isJunction : null);
			event.setCanceled(true);
		});

		ms.popPose();
	}

	private static void renderShape(VoxelShape s, PoseStack ms, VertexConsumer vb, Boolean valid) {
		PoseStack.Pose transform = ms.last();
		s.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
			float xDiff = (float) (x2 - x1);
			float yDiff = (float) (y2 - y1);
			float zDiff = (float) (z2 - z1);
			float length = Mth.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);

			xDiff /= length;
			yDiff /= length;
			zDiff /= length;

			float r = 0f;
			float g = 0f;
			float b = 0f;

			if (valid != null && valid) {
				g = 1f;
				b = 1f;
				r = 1f;
			}

			if (valid != null && !valid) {
				r = 1f;
				b = 0.125f;
				g = 0.25f;
			}

			vb.vertex(transform.pose(), (float) x1, (float) y1, (float) z1)
				.color(r, g, b, .4f)
				.normal(transform.normal(), xDiff, yDiff, zDiff)
				.endVertex();
			vb.vertex(transform.pose(), (float) x2, (float) y2, (float) z2)
				.color(r, g, b, .4f)
				.normal(transform.normal(), xDiff, yDiff, zDiff)
				.endVertex();

		});
	}

	private static final VoxelShape LONG_CROSS =
		Shapes.or(TrackVoxelShapes.longOrthogonalZ(), TrackVoxelShapes.longOrthogonalX());
	private static final VoxelShape LONG_ORTHO = TrackVoxelShapes.longOrthogonalZ();
	private static final VoxelShape LONG_ORTHO_OFFSET = TrackVoxelShapes.longOrthogonalZOffset();

	private static void walkShapes(TrackShape shape, TransformStack msr, Consumer<VoxelShape> renderer) {
		float angle45 = Mth.PI / 4;

		if (shape == TrackShape.XO || shape == TrackShape.CR_NDX || shape == TrackShape.CR_PDX)
			renderer.accept(AllShapes.TRACK_ORTHO.get(Direction.EAST));
		else if (shape == TrackShape.ZO || shape == TrackShape.CR_NDZ || shape == TrackShape.CR_PDZ)
			renderer.accept(AllShapes.TRACK_ORTHO.get(Direction.SOUTH));

		if (shape.isPortal()) {
			for (Direction d : Iterate.horizontalDirections) {
				if (TrackShape.asPortal(d) != shape)
					continue;
				msr.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(d)));
				renderer.accept(LONG_ORTHO_OFFSET);
				return;
			}
		}

		if (shape == TrackShape.PD || shape == TrackShape.CR_PDX || shape == TrackShape.CR_PDZ) {
			msr.rotateCentered(Direction.UP, angle45);
			renderer.accept(LONG_ORTHO);
		} else if (shape == TrackShape.ND || shape == TrackShape.CR_NDX || shape == TrackShape.CR_NDZ) {
			msr.rotateCentered(Direction.UP, -Mth.PI / 4);
			renderer.accept(LONG_ORTHO);
		}

		if (shape == TrackShape.CR_O)
			renderer.accept(AllShapes.TRACK_CROSS);
		else if (shape == TrackShape.CR_D) {
			msr.rotateCentered(Direction.UP, angle45);
			renderer.accept(LONG_CROSS);
		}

		if (!(shape == TrackShape.AE || shape == TrackShape.AN || shape == TrackShape.AW || shape == TrackShape.AS))
			return;

		msr.translate(0, 1, 0);
		msr.rotateCentered(Direction.UP, Mth.PI - AngleHelper.rad(shape.getModelRotation()));
		msr.rotateXRadians(angle45);
		msr.translate(0, -3 / 16f, 1 / 16f);
		renderer.accept(LONG_ORTHO);
	}

	public static record BezierPointSelection(TrackTileEntity te, BezierTrackPointLocation loc, Vec3 vec, Vec3 angles,
		Vec3 direction) {
	}

}
