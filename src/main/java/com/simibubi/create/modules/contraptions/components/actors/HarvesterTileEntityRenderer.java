package com.simibubi.create.modules.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.block.SafeTileEntityRendererFast;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.IHaveMovementBehavior.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HarvesterTileEntityRenderer extends SafeTileEntityRendererFast<HarvesterTileEntity> {

	@Override
	public void renderFast(HarvesterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		SuperByteBuffer superBuffer = renderHead(getWorld(), te.getPos(), te.getBlockState(), 0);
		superBuffer.translate(x, y, z).renderInto(buffer);
	}

	public static SuperByteBuffer renderInContraption(MovementContext context) {
		BlockState state = context.state;
		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, state.get(HORIZONTAL_FACING).getOpposite())
				? context.getAnimationSpeed() * state.get(HORIZONTAL_FACING).getAxisDirection().getOffset()
				: 0);
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (float) (((time * speed) % 360) / 180 * (float) Math.PI);

		return renderHead(context.world, BlockPos.ZERO, state, angle);
	}

	public static SuperByteBuffer renderHead(World world, BlockPos pos, BlockState state, float angle) {
		SuperByteBuffer buffer = AllBlockPartials.HARVESTER_BLADE.renderOnHorizontal(state);
		int lightMapCoords = state.getPackedLightmapCoords(world, pos);
		Direction facing = state.get(HORIZONTAL_FACING);
		Axis axis = facing.rotateYCCW().getAxis();
		int axisDirection = -facing.getAxisDirection().getOffset();
		float originOffset = 1 / 16f;
		float xOffset = axis == Axis.X ? 0 : originOffset * axisDirection;
		float zOffset = axis == Axis.Z ? 0 : originOffset * axisDirection;

		buffer.translate(xOffset, 2 * originOffset, zOffset);
		buffer.rotateCentered(axis, angle);
		buffer.translate(-xOffset, -2 * originOffset, -zOffset);

		buffer.light(lightMapCoords);
		return buffer;
	}

}
