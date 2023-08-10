package com.simibubi.create.content.kinetics.clock;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.clock.CuckooClockBlockEntity.Animation;
import com.simibubi.create.foundation.render.CachedPartialBuffers;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class CuckooClockRenderer extends KineticBlockEntityRenderer<CuckooClockBlockEntity> {

	public CuckooClockRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(CuckooClockBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		if (!(be instanceof CuckooClockBlockEntity))
			return;

		BlockState blockState = be.getBlockState();
		Direction direction = blockState.getValue(CuckooClockBlock.HORIZONTAL_FACING);

		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		// Render Hands
		SuperByteBuffer hourHand = CachedPartialBuffers.partial(AllPartialModels.CUCKOO_HOUR_HAND, blockState);
		SuperByteBuffer minuteHand = CachedPartialBuffers.partial(AllPartialModels.CUCKOO_MINUTE_HAND, blockState);
		float hourAngle = be.hourHand.getValue(partialTicks);
		float minuteAngle = be.minuteHand.getValue(partialTicks);
		rotateHand(hourHand, hourAngle, direction).light(light)
				.renderInto(ms, vb);
		rotateHand(minuteHand, minuteAngle, direction).light(light)
				.renderInto(ms, vb);

		// Doors
		SuperByteBuffer leftDoor = CachedPartialBuffers.partial(AllPartialModels.CUCKOO_LEFT_DOOR, blockState);
		SuperByteBuffer rightDoor = CachedPartialBuffers.partial(AllPartialModels.CUCKOO_RIGHT_DOOR, blockState);
		float angle = 0;
		float offset = 0;

		if (be.animationType != null) {
			float value = be.animationProgress.getValue(partialTicks);
			int step = be.animationType == Animation.SURPRISE ? 3 : 15;
			for (int phase = 30; phase <= 60; phase += step) {
				float local = value - phase;
				if (local < -step / 3)
					continue;
				else if (local < 0)
					angle = Mth.lerp(((value - (phase - 5)) / 5), 0, 135);
				else if (local < step / 3)
					angle = 135;
				else if (local < 2 * step / 3)
					angle = Mth.lerp(((value - (phase + 5)) / 5), 135, 0);

			}
		}

		rotateDoor(leftDoor, angle, true, direction).light(light)
			.renderInto(ms, vb);
		rotateDoor(rightDoor, angle, false, direction).light(light)
			.renderInto(ms, vb);

		// Figure
		if (be.animationType != Animation.NONE) {
			offset = -(angle / 135) * 1 / 2f + 10 / 16f;
			PartialModel partialModel = (be.animationType == Animation.PIG ? AllPartialModels.CUCKOO_PIG : AllPartialModels.CUCKOO_CREEPER);
			SuperByteBuffer figure =
					CachedPartialBuffers.partial(partialModel, blockState);
			figure.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(direction.getCounterClockWise())));
			figure.translate(offset, 0, 0);
			figure.light(light)
					.renderInto(ms, vb);
		}

	}

	@Override
	protected SuperByteBuffer getRotatedModel(CuckooClockBlockEntity be, BlockState state) {
		return CachedPartialBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state
				.getValue(CuckooClockBlock.HORIZONTAL_FACING)
				.getOpposite());
	}

	private SuperByteBuffer rotateHand(SuperByteBuffer buffer, float angle, Direction facing) {
		float pivotX = 2 / 16f;
		float pivotY = 6 / 16f;
		float pivotZ = 8 / 16f;
		buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing.getCounterClockWise())));
		buffer.translate(pivotX, pivotY, pivotZ);
		buffer.rotate(Direction.EAST, AngleHelper.rad(angle));
		buffer.translate(-pivotX, -pivotY, -pivotZ);
		return buffer;
	}

	private SuperByteBuffer rotateDoor(SuperByteBuffer buffer, float angle, boolean left, Direction facing) {
		float pivotX = 2 / 16f;
		float pivotY = 0;
		float pivotZ = (left ? 6 : 10) / 16f;
		buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing.getCounterClockWise())));
		buffer.translate(pivotX, pivotY, pivotZ);
		buffer.rotate(Direction.UP, AngleHelper.rad(angle) * (left ? -1 : 1));
		buffer.translate(-pivotX, -pivotY, -pivotZ);
		return buffer;
	}

}
