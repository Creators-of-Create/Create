package com.simibubi.create.modules.contraptions.components.clock;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.clock.CuckooClockTileEntity.Animation;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class CuckooClockRenderer extends KineticTileEntityRenderer {

	public CuckooClockRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		if (!(te instanceof CuckooClockTileEntity))
			return;

		CuckooClockTileEntity clock = (CuckooClockTileEntity) te;
		BlockState blockState = te.getBlockState();
		int packedLightmapCoords =WorldRenderer.getLightmapCoordinates(te.getWorld(), blockState, te.getPos());
		Direction direction = blockState.get(CuckooClockBlock.HORIZONTAL_FACING);
		
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());

		// Render Hands
		SuperByteBuffer hourHand = AllBlockPartials.CUCKOO_HOUR_HAND.renderOn(blockState);
		SuperByteBuffer minuteHand = AllBlockPartials.CUCKOO_MINUTE_HAND.renderOn(blockState);
		float hourAngle = clock.hourHand.get(partialTicks);
		float minuteAngle = clock.minuteHand.get(partialTicks);
		rotateHand(hourHand, hourAngle, direction).light(packedLightmapCoords).renderInto(ms, vb);
		rotateHand(minuteHand, minuteAngle, direction).light(packedLightmapCoords).renderInto(ms, vb);

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

		rotateDoor(leftDoor, angle, true, direction).light(packedLightmapCoords).renderInto(ms, vb);
		rotateDoor(rightDoor, angle, false, direction).light(packedLightmapCoords).renderInto(ms, vb);

		// Figure
		if (clock.animationType != null) {
			offset = -(angle / 135) * 1 / 2f + 10 / 16f;
			SuperByteBuffer figure = (clock.animationType == Animation.PIG ? AllBlockPartials.CUCKOO_PIG
					: AllBlockPartials.CUCKOO_CREEPER).renderOn(blockState);
			figure.translate(offset, 0, 0);
			figure.rotateCentered(Axis.Y, AngleHelper.rad(AngleHelper.horizontalAngle(direction.rotateYCCW())));
			figure.light(packedLightmapCoords).renderInto(ms, vb);
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
