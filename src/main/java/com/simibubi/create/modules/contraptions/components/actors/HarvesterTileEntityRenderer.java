package com.simibubi.create.modules.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HarvesterTileEntityRenderer extends SafeTileEntityRenderer<HarvesterTileEntity> {

	public HarvesterTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(HarvesterTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		SuperByteBuffer superBuffer = renderHead(te.getWorld(), te.getPos(), te.getBlockState(), 0);
		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

	public static SuperByteBuffer renderInContraption(MovementContext context) {
		BlockState state = context.state;
		Direction facing = state.get(HORIZONTAL_FACING);
		int offset = facing.getAxisDirection().getOffset() * (facing.getAxis() == Axis.X ? 1 : -1);
		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite())
				? context.getAnimationSpeed() * offset
				: 0);
		if (context.contraption.stalled)
			speed = 0;
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (float) (((time * speed) % 360) / 180 * (float) Math.PI);

		return renderHead(context.world, BlockPos.ZERO, state, angle);
	}

	public static SuperByteBuffer renderHead(World world, BlockPos pos, BlockState state, float angle) {
		SuperByteBuffer buffer = AllBlockPartials.HARVESTER_BLADE.renderOnHorizontal(state);
		int lightMapCoords = WorldRenderer.getLightmapCoordinates(world, state, pos);
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
