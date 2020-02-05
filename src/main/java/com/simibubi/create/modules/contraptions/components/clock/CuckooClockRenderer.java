package com.simibubi.create.modules.contraptions.components.clock;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.clock.CuckooClockTileEntity.Animation;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class CuckooClockRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		super.renderFast(te, x, y, z, partialTicks, destroyStage, buffer);
		if (!(te instanceof CuckooClockTileEntity))
			return;

		CuckooClockTileEntity clock = (CuckooClockTileEntity) te;
		BlockState blockState = te.getBlockState();
		int light = blockState.getPackedLightmapCoords(getWorld(), te.getPos());
		Direction direction = blockState.get(CuckooClockBlock.HORIZONTAL_FACING);

		// Render Hands
		SuperByteBuffer hourHand = AllBlockPartials.CUCKOO_HOUR_HAND.renderOn(blockState);
		SuperByteBuffer minuteHand = AllBlockPartials.CUCKOO_MINUTE_HAND.renderOn(blockState);
		float hourAngle = clock.hourHand.get(partialTicks);
		float minuteAngle = clock.minuteHand.get(partialTicks);
		rotateHand(hourHand, hourAngle, direction).translate(x, y, z).light(light).renderInto(buffer);
		rotateHand(minuteHand, minuteAngle, direction).translate(x, y, z).light(light).renderInto(buffer);

		// Doors
		SuperByteBuffer leftDoor = AllBlockPartials.CUCKOO_LEFT_DOOR.renderOn(blockState);
		SuperByteBuffer rightDoor = AllBlockPartials.CUCKOO_RIGHT_DOOR.renderOn(blockState);
		float angle = 0;
		float offset = 0;

		if (clock.animationType != null) {
			float value = clock.animationProgress.get(partialTicks);
			int step = clock.animationType == Animation.SURPRISE ? 3 : 15;
			for (int phase = 30; phase <= 60; phase += step) {
				float local = value - phase;
				if (local < -step / 3)
					continue;
				else if (local < 0)
					angle = MathHelper.lerp(((value - (phase - 5)) / 5), 0, 135);
				else if (local < step / 3)
					angle = 135;
				else if (local < 2 * step / 3)
					angle = MathHelper.lerp(((value - (phase + 5)) / 5), 135, 0);

			}
		}

		rotateDoor(leftDoor, angle, true, direction).translate(x, y, z).light(light).renderInto(buffer);
		rotateDoor(rightDoor, angle, false, direction).translate(x, y, z).light(light).renderInto(buffer);

		// Figure
		if (clock.animationType != null) {
			offset = -(angle / 135) * 1 / 2f + 10 / 16f;
			SuperByteBuffer figure = (clock.animationType == Animation.PIG ? AllBlockPartials.CUCKOO_PIG
					: AllBlockPartials.CUCKOO_CREEPER).renderOn(blockState);
			figure.translate(offset, 0, 0);
			figure.rotateCentered(Axis.Y, AngleHelper.rad(AngleHelper.horizontalAngle(direction.rotateYCCW())));
			figure.translate(x, y, z).light(light).renderInto(buffer);
		}

	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return transform(AllBlockPartials.SHAFT_HALF, te);
	}

	private SuperByteBuffer transform(AllBlockPartials partial, KineticTileEntity te) {
		return partial.renderOnDirectional(te.getBlockState(),
				te.getBlockState().get(CuckooClockBlock.HORIZONTAL_FACING).getOpposite());
	}

	private SuperByteBuffer rotateHand(SuperByteBuffer buffer, float angle, Direction facing) {
		float pivotX = 2 / 16f;
		float pivotY = 6 / 16f;
		float pivotZ = 8 / 16f;
		buffer.translate(-pivotX, -pivotY, -pivotZ);
		buffer.rotate(Axis.X, angle);
		buffer.translate(pivotX, pivotY, pivotZ);
		buffer.rotateCentered(Axis.Y, AngleHelper.rad(AngleHelper.horizontalAngle(facing.rotateYCCW())));
		return buffer;
	}

	private SuperByteBuffer rotateDoor(SuperByteBuffer buffer, float angle, boolean left, Direction facing) {
		float pivotX = 2 / 16f;
		float pivotY = 0;
		float pivotZ = (left ? 6 : 10) / 16f;
		buffer.translate(-pivotX, -pivotY, -pivotZ);
		buffer.rotate(Axis.Y, AngleHelper.rad(angle) * (left ? -1 : 1));
		buffer.translate(pivotX, pivotY, pivotZ);
		buffer.rotateCentered(Axis.Y, AngleHelper.rad(AngleHelper.horizontalAngle(facing.rotateYCCW())));
		return buffer;
	}

}
