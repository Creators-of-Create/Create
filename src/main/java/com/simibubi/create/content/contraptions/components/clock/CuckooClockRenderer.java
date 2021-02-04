package com.simibubi.create.content.contraptions.components.clock;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockTileEntity.Animation;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
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
		int packedLightmapCoords = WorldRenderer.getLightmapCoordinates(te.getWorld(), blockState, te.getPos());
		Direction direction = blockState.get(CuckooClockBlock.HORIZONTAL_FACING);

		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());

		// Render Hands
		SuperByteBuffer hourHand = AllBlockPartials.CUCKOO_HOUR_HAND.renderOn(blockState);
		SuperByteBuffer minuteHand = AllBlockPartials.CUCKOO_MINUTE_HAND.renderOn(blockState);
		float hourAngle = clock.hourHand.get(partialTicks);
		float minuteAngle = clock.minuteHand.get(partialTicks);
		rotateHand(hourHand, hourAngle, direction).light(packedLightmapCoords)
			.renderInto(ms, vb);
		rotateHand(minuteHand, minuteAngle, direction).light(packedLightmapCoords)
			.renderInto(ms, vb);

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

		rotateDoor(leftDoor, angle, true, direction).light(packedLightmapCoords)
			.renderInto(ms, vb);
		rotateDoor(rightDoor, angle, false, direction).light(packedLightmapCoords)
			.renderInto(ms, vb);

		// Figure
		if (clock.animationType != Animation.NONE) {
			offset = -(angle / 135) * 1 / 2f + 10 / 16f;
			SuperByteBuffer figure =
				(clock.animationType == Animation.PIG ? AllBlockPartials.CUCKOO_PIG : AllBlockPartials.CUCKOO_CREEPER)
					.renderOn(blockState);
			figure.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(direction.rotateYCCW())));
			figure.translate(offset, 0, 0);
			figure.light(packedLightmapCoords)
				.renderInto(ms, vb);
		}

	}

	private SuperByteBuffer rotateHand(SuperByteBuffer buffer, float angle, Direction facing) {
		float pivotX = 2 / 16f;
		float pivotY = 6 / 16f;
		float pivotZ = 8 / 16f;
		buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing.rotateYCCW())));
		buffer.translate(pivotX, pivotY, pivotZ);
		buffer.rotate(Direction.EAST, angle);
		buffer.translate(-pivotX, -pivotY, -pivotZ);
		return buffer;
	}

	private SuperByteBuffer rotateDoor(SuperByteBuffer buffer, float angle, boolean left, Direction facing) {
		float pivotX = 2 / 16f;
		float pivotY = 0;
		float pivotZ = (left ? 6 : 10) / 16f;
		buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing.rotateYCCW())));
		buffer.translate(pivotX, pivotY, pivotZ);
		buffer.rotate(Direction.UP, AngleHelper.rad(angle) * (left ? -1 : 1));
		buffer.translate(-pivotX, -pivotY, -pivotZ);
		return buffer;
	}

}
