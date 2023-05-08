package com.simibubi.create.content.logistics.trains.management.edgePoint;

import com.google.common.base.Objects;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.GraphLocation;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBlockItem.OverlapResult;
import com.simibubi.create.content.logistics.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.logistics.trains.track.TrackBlockOutline;
import com.simibubi.create.content.logistics.trains.track.TrackBlockOutline.BezierPointSelection;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class TrackTargetingClient {

	static BlockPos lastHovered;
	static boolean lastDirection;
	static EdgePointType<?> lastType;
	static BezierTrackPointLocation lastHoveredBezierSegment;

	static OverlapResult lastResult;
	static GraphLocation lastLocation;

	public static void clientTick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		Vec3 lookAngle = player.getLookAngle();

		BlockPos hovered = null;
		boolean direction = false;
		EdgePointType<?> type = null;
		BezierTrackPointLocation hoveredBezier = null;

		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof TrackTargetingBlockItem ttbi)
			type = ttbi.getType(stack);

		if (type == EdgePointType.SIGNAL)
			Create.RAILWAYS.sided(null)
				.tickSignalOverlay();

		boolean alreadySelected = stack.hasTag() && stack.getTag()
			.contains("SelectedPos");

		if (type != null) {
			BezierPointSelection bezierSelection = TrackBlockOutline.result;

			if (alreadySelected) {
				CompoundTag tag = stack.getTag();
				hovered = NbtUtils.readBlockPos(tag.getCompound("SelectedPos"));
				direction = tag.getBoolean("SelectedDirection");
				if (tag.contains("Bezier")) {
					CompoundTag bezierNbt = tag.getCompound("Bezier");
					BlockPos key = NbtUtils.readBlockPos(bezierNbt.getCompound("Key"));
					hoveredBezier = new BezierTrackPointLocation(key, bezierNbt.getInt("Segment"));
				}

			} else if (bezierSelection != null) {
				hovered = bezierSelection.blockEntity()
					.getBlockPos();
				hoveredBezier = bezierSelection.loc();
				direction = lookAngle.dot(bezierSelection.direction()) < 0;

			} else {
				HitResult hitResult = mc.hitResult;
				if (hitResult != null && hitResult.getType() == Type.BLOCK) {
					BlockHitResult blockHitResult = (BlockHitResult) hitResult;
					BlockPos pos = blockHitResult.getBlockPos();
					BlockState blockState = mc.level.getBlockState(pos);
					if (blockState.getBlock() instanceof ITrackBlock track) {
						direction = track.getNearestTrackAxis(mc.level, pos, blockState, lookAngle)
							.getSecond() == AxisDirection.POSITIVE;
						hovered = pos;
					}
				}
			}
		}

		if (hovered == null) {
			lastHovered = null;
			lastResult = null;
			lastLocation = null;
			lastHoveredBezierSegment = null;
			return;
		}

		if (Objects.equal(hovered, lastHovered) && Objects.equal(hoveredBezier, lastHoveredBezierSegment)
			&& direction == lastDirection && type == lastType)
			return;

		lastType = type;
		lastHovered = hovered;
		lastDirection = direction;
		lastHoveredBezierSegment = hoveredBezier;

		TrackTargetingBlockItem.withGraphLocation(mc.level, hovered, direction, hoveredBezier, type,
			(result, location) -> {
				lastResult = result;
				lastLocation = location;
			});
	}

	public static void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
		if (lastLocation == null || lastResult.feedback != null)
			return;

		Minecraft mc = Minecraft.getInstance();
		BlockPos pos = lastHovered;
		int light = LevelRenderer.getLightColor(mc.level, pos);
		AxisDirection direction = lastDirection ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;

		RenderedTrackOverlayType type = lastType == EdgePointType.SIGNAL ? RenderedTrackOverlayType.SIGNAL
			: lastType == EdgePointType.OBSERVER ? RenderedTrackOverlayType.OBSERVER : RenderedTrackOverlayType.STATION;

		ms.pushPose();
		TransformStack.cast(ms)
			.translate(Vec3.atLowerCornerOf(pos)
				.subtract(camera));
		TrackTargetingBehaviour.render(mc.level, pos, direction, lastHoveredBezierSegment, ms, buffer, light,
			OverlayTexture.NO_OVERLAY, type, 1 + 1 / 16f);
		ms.popPose();
	}

}
