package com.simibubi.create.modules.contraptions.receivers;

import static com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer.KINETIC_TILE;
import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.receivers.contraptions.IHaveMovementBehavior.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HarvesterTileEntityRenderer extends TileEntityRenderer<HarvesterTileEntity> {

	@Override
	public void renderTileEntityFast(HarvesterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		SuperByteBuffer superBuffer = renderHead(getWorld(), te.getPos(), te.getBlockState(), 0);
		superBuffer.translate(x, y, z).renderInto(buffer);
	}

	public static SuperByteBuffer renderInContraption(MovementContext context) {
		BlockState state = context.state;
		Direction facing = context.getMovementDirection();
		float speed = -500 * state.get(HORIZONTAL_FACING).getAxisDirection().getOffset();
//		float speed = (float) (facing != state.get(HORIZONTAL_FACING)
//				? context.getAnimationSpeed() * facing.getAxisDirection().getOffset()
//				: 0);
//		if (facing.getAxis() == Axis.X)
//			speed = -speed;
		float time = AnimationTickHolder.getRenderTick();
		float angle = (float) (((time * speed) % 360) / 180 * (float) Math.PI);

		return renderHead(context.world, BlockPos.ZERO, state, angle);
	}

	public static SuperByteBuffer renderHead(World world, BlockPos pos, BlockState state, float angle) {
		BlockState renderedState = AllBlocks.HARVESTER_BLADE.get().getDefaultState().with(HORIZONTAL_FACING,
				state.get(HORIZONTAL_FACING));
		SuperByteBuffer buffer = CreateClient.bufferCache.renderBlockState(KINETIC_TILE, renderedState);

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
