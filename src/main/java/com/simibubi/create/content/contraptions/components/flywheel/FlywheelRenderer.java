package com.simibubi.create.content.contraptions.components.flywheel;

import static com.simibubi.create.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelBlock.ConnectionState;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MathHelper;

public class FlywheelRenderer extends KineticTileEntityRenderer {

	public FlywheelRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (FastRenderDispatcher.available(te.getWorld())) return;

		BlockState blockState = te.getBlockState();
		FlywheelTileEntity wte = (FlywheelTileEntity) te;

		float speed = wte.visualSpeed.get(partialTicks) * 3 / 10f;
		float angle = wte.angle + speed * partialTicks;

		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());

		if (FlywheelBlock.isConnected(blockState)) {
			Direction connection = FlywheelBlock.getConnection(blockState);
			light = WorldRenderer.getLightmapCoordinates(te.getWorld(), blockState, te.getPos()
				.offset(connection));
			float rotation =
				connection.getAxis() == Axis.X ^ connection.getAxisDirection() == AxisDirection.NEGATIVE ? -angle
					: angle;
			boolean flip = blockState.get(FlywheelBlock.CONNECTION) == ConnectionState.LEFT;

			transformConnector(
					rotateToFacing(PartialBufferer.get(AllBlockPartials.FLYWHEEL_UPPER_ROTATING, blockState), connection), true, true,
					rotation, flip).light(light)
					.renderInto(ms, vb);
			transformConnector(
					rotateToFacing(PartialBufferer.get(AllBlockPartials.FLYWHEEL_LOWER_ROTATING, blockState), connection), false, true,
					rotation, flip).light(light)
					.renderInto(ms, vb);

			transformConnector(rotateToFacing(PartialBufferer.get(AllBlockPartials.FLYWHEEL_UPPER_SLIDING, blockState), connection),
					true, false, rotation, flip).light(light)
					.renderInto(ms, vb);
			transformConnector(rotateToFacing(PartialBufferer.get(AllBlockPartials.FLYWHEEL_LOWER_SLIDING, blockState), connection),
					false, false, rotation, flip).light(light)
					.renderInto(ms, vb);
		}

		SuperByteBuffer wheel = PartialBufferer.getHorizontal(AllBlockPartials.FLYWHEEL, blockState.rotate(Rotation.CLOCKWISE_90));
		kineticRotationTransform(wheel, te, blockState.get(HORIZONTAL_FACING)
				.getAxis(), AngleHelper.rad(angle), light);
		wheel.renderInto(ms, vb);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return PartialBufferer.getDirectionalSouth(AllBlockPartials.SHAFT_HALF, te.getBlockState(), te.getBlockState()
				.get(BlockStateProperties.HORIZONTAL_FACING)
				.getOpposite());
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

		buffer.translate(pivotX, pivotY, pivotZ + shifting);
		if (rotating)
			buffer.rotate(Direction.EAST, AngleHelper.rad(barAngle));
		buffer.translate(-pivotX, -pivotY, -pivotZ);

		if (flip && !upper)
			buffer.translate(9 / 16f, 0, 0);

		return buffer;
	}

	protected SuperByteBuffer rotateToFacing(SuperByteBuffer buffer, Direction facing) {
		buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)));
		return buffer;
	}

}
