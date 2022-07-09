package com.simibubi.create.content.contraptions.components.clock;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockTileEntity.Animation;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class CuckooClockRenderer extends KineticTileEntityRenderer {

	public CuckooClockRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		if (!(te instanceof CuckooClockTileEntity))
			return;

		CuckooClockTileEntity clock = (CuckooClockTileEntity) te;
		BlockState blockState = te.getBlockState();
		Direction direction = blockState.getValue(CuckooClockBlock.HORIZONTAL_FACING);

		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		// Render Hands
		SuperByteBuffer hourHand = CachedBufferer.partial(AllBlockPartials.CUCKOO_HOUR_HAND, blockState);
		SuperByteBuffer minuteHand = CachedBufferer.partial(AllBlockPartials.CUCKOO_MINUTE_HAND, blockState);
		float hourAngle = clock.hourHand.getValue(partialTicks);
		float minuteAngle = clock.minuteHand.getValue(partialTicks);
		rotateHand(hourHand, hourAngle, direction).light(light)
				.renderInto(ms, vb);
		rotateHand(minuteHand, minuteAngle, direction).light(light)
				.renderInto(ms, vb);

		// Doors
		SuperByteBuffer leftDoor = CachedBufferer.partial(AllBlockPartials.CUCKOO_LEFT_DOOR, blockState);
		SuperByteBuffer rightDoor = CachedBufferer.partial(AllBlockPartials.CUCKOO_RIGHT_DOOR, blockState);
		float angle = 0;
		float offset = 0;

		if (clock.animationType != null) {
			float value = clock.animationProgress.getValue(partialTicks);
			int step = clock.animationType == Animation.SURPRISE ? 3 : 15;
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
		if (clock.animationType != Animation.NONE) {
			offset = -(angle / 135) * 1 / 2f + 10 / 16f;
			PartialModel partialModel = (clock.animationType == Animation.PIG ? AllBlockPartials.CUCKOO_PIG : AllBlockPartials.CUCKOO_CREEPER);
			SuperByteBuffer figure =
					CachedBufferer.partial(partialModel, blockState);
			figure.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(direction.getCounterClockWise())));
			figure.translate(offset, 0, 0);
			figure.light(light)
					.renderInto(ms, vb);
		}

	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te, BlockState state) {
		return CachedBufferer.partialFacing(AllBlockPartials.SHAFT_HALF, state, state
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
