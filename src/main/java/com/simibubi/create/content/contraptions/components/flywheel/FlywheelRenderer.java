package com.simibubi.create.content.contraptions.components.flywheel;

import static com.simibubi.create.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelBlock.ConnectionState;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FlywheelRenderer extends KineticTileEntityRenderer {

	public FlywheelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		BlockState blockState = te.getBlockState();
		FlywheelTileEntity wte = (FlywheelTileEntity) te;

		float speed = wte.visualSpeed.get(partialTicks) * 3 / 10f;
		float angle = wte.angle + speed * partialTicks;

		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		if (FlywheelBlock.isConnected(blockState)) {
			Direction connection = FlywheelBlock.getConnection(blockState);
			light = LevelRenderer.getLightColor(te.getLevel(), blockState, te.getBlockPos()
				.relative(connection));
			float rotation =
				connection.getAxis() == Axis.X ^ connection.getAxisDirection() == AxisDirection.NEGATIVE ? -angle
					: angle;
			boolean flip = blockState.getValue(FlywheelBlock.CONNECTION) == ConnectionState.LEFT;

			transformConnector(
					rotateToFacing(CachedBufferer.partial(AllBlockPartials.FLYWHEEL_UPPER_ROTATING, blockState), connection), true, true,
					rotation, flip).light(light)
					.renderInto(ms, vb);
			transformConnector(
					rotateToFacing(CachedBufferer.partial(AllBlockPartials.FLYWHEEL_LOWER_ROTATING, blockState), connection), false, true,
					rotation, flip).light(light)
					.renderInto(ms, vb);

			transformConnector(rotateToFacing(CachedBufferer.partial(AllBlockPartials.FLYWHEEL_UPPER_SLIDING, blockState), connection),
					true, false, rotation, flip).light(light)
					.renderInto(ms, vb);
			transformConnector(rotateToFacing(CachedBufferer.partial(AllBlockPartials.FLYWHEEL_LOWER_SLIDING, blockState), connection),
					false, false, rotation, flip).light(light)
					.renderInto(ms, vb);
		}

		renderFlywheel(te, ms, light, blockState, angle, vb);
	}

	private void renderFlywheel(KineticTileEntity te, PoseStack ms, int light, BlockState blockState, float angle, VertexConsumer vb) {
		BlockState referenceState = blockState.rotate(Rotation.CLOCKWISE_90);
		Direction facing = referenceState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		SuperByteBuffer wheel = CachedBufferer.partialFacing(AllBlockPartials.FLYWHEEL, referenceState, facing);
		kineticRotationTransform(wheel, te, blockState.getValue(HORIZONTAL_FACING)
				.getAxis(), AngleHelper.rad(angle), light);
		wheel.renderInto(ms, vb);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return CachedBufferer.partialFacing(AllBlockPartials.SHAFT_HALF, te.getBlockState(), te.getBlockState()
				.getValue(BlockStateProperties.HORIZONTAL_FACING)
				.getOpposite());
	}

	protected SuperByteBuffer transformConnector(SuperByteBuffer buffer, boolean upper, boolean rotating, float angle,
		boolean flip) {

		float shift = upper ? 1 / 4f : -1 / 8f;
		float offset = upper ? 1 / 4f : 1 / 4f;
		float radians = (float) (angle / 180 * Math.PI);
		float shifting = Mth.sin(radians) * shift + offset;

		float maxAngle = upper ? -5 : -15;
		float minAngle = upper ? -45 : 5;
		float barAngle = 0;

		if (rotating)
			barAngle = Mth.lerp((Mth.sin((float) (radians + Math.PI / 2)) + 1) / 2, minAngle, maxAngle);

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
