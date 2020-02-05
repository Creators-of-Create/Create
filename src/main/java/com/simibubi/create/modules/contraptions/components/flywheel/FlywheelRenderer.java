package com.simibubi.create.modules.contraptions.components.flywheel;

import static com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.flywheel.FlywheelBlock.ConnectionState;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Rotation;

public class FlywheelRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		super.renderFast(te, x, y, z, partialTicks, destroyStage, buffer);

		BlockState blockState = te.getBlockState();
		FlywheelTileEntity wte = (FlywheelTileEntity) te;

		SuperByteBuffer wheel = AllBlockPartials.FLYWHEEL.renderOnHorizontal(blockState.rotate(Rotation.CLOCKWISE_90));
		float speed = wte.visualSpeed.get(partialTicks) * 3 / 10f;
		float angle = wte.angle + speed * partialTicks;

		if (FlywheelBlock.isConnected(blockState)) {
			Direction connection = FlywheelBlock.getConnection(blockState);
			int light = blockState.getPackedLightmapCoords(getWorld(), te.getPos().offset(connection));
			float rotation = connection.getAxis() == Axis.X ^ connection.getAxisDirection() == AxisDirection.NEGATIVE
					? -angle
					: angle;
			boolean flip = blockState.get(FlywheelBlock.CONNECTION) == ConnectionState.LEFT;

			rotateToFacing(transformConnector(AllBlockPartials.FLYWHEEL_UPPER_ROTATING.renderOn(blockState), true, true,
					rotation, flip), connection).translate(x, y, z).light(light).renderInto(buffer);
			rotateToFacing(transformConnector(AllBlockPartials.FLYWHEEL_LOWER_ROTATING.renderOn(blockState), false,
					true, rotation, flip), connection).translate(x, y, z).light(light).renderInto(buffer);
			rotateToFacing(transformConnector(AllBlockPartials.FLYWHEEL_UPPER_SLIDING.renderOn(blockState), true, false,
					rotation, flip), connection).translate(x, y, z).light(light).renderInto(buffer);
			rotateToFacing(transformConnector(AllBlockPartials.FLYWHEEL_LOWER_SLIDING.renderOn(blockState), false,
					false, rotation, flip), connection).translate(x, y, z).light(light).renderInto(buffer);
		}

		kineticRotationTransform(wheel, te, blockState.get(HORIZONTAL_FACING).getAxis(), AngleHelper.rad(angle),
				getWorld());
		wheel.translate(x, y, z).renderInto(buffer);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.SHAFT_HALF.renderOnDirectional(te.getBlockState(),
				te.getBlockState().get(HORIZONTAL_FACING).getOpposite());
	}

	protected SuperByteBuffer transformConnector(SuperByteBuffer buffer, boolean upper, boolean rotating, float angle,
			boolean flip) {

		float shift = upper ? 1 / 4f : -1 / 8f;
		float offset = upper ? 1 / 4f : 1 / 4f;
		float radians = (float) (angle / 180 * Math.PI);
		float shifting = MathHelper.sin(radians) * shift + offset;

		float maxAngle = upper ? -5 : -15;
		float minAngle = upper ? -45 : 5;
		float barAngle = 0;

		if (rotating)
			barAngle = MathHelper.lerp((MathHelper.sin((float) (radians + Math.PI / 2)) + 1) / 2, minAngle, maxAngle);

		float pivotX = (upper ? 8f : 3f) / 16;
		float pivotY = (upper ? 8f : 2f) / 16;
		float pivotZ = (upper ? 23f : 21.5f) / 16f;

		if (flip && !upper)
			buffer.translate(9 / 16f, 0, 0);

		buffer.translate(-pivotX, -pivotY, -pivotZ);
		if (rotating)
			buffer.rotate(Axis.X, AngleHelper.rad(barAngle));
		buffer.translate(pivotX, pivotY, pivotZ + shifting);

		return buffer;
	}

	protected SuperByteBuffer rotateToFacing(SuperByteBuffer buffer, Direction facing) {
		buffer.rotateCentered(Axis.Y, AngleHelper.rad(AngleHelper.horizontalAngle(facing)));
		return buffer;
	}

}
