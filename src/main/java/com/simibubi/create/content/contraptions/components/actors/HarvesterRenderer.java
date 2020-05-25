package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class HarvesterRenderer extends SafeTileEntityRenderer<HarvesterTileEntity> {

	public HarvesterRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(HarvesterTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();

		SuperByteBuffer superBuffer = AllBlockPartials.HARVESTER_BLADE.renderOnHorizontal(blockState);
		transformHead(ms, 0);
		superBuffer.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.getCutoutMipped()));
	}

	public static void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffers) {
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };
		BlockState blockState = context.state;
		Direction facing = blockState.get(HORIZONTAL_FACING);
		SuperByteBuffer superBuffer = AllBlockPartials.HARVESTER_BLADE.renderOnHorizontal(blockState);
		int offset = facing.getAxisDirection()
			.getOffset() * (facing.getAxis() == Axis.X ? 1 : -1);
		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite())
			? context.getAnimationSpeed() * offset
			: 0);
		if (context.contraption.stalled)
			speed = 0;
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (time * speed) % 360;

		for (MatrixStack m : matrixStacks) 
			transformHead(m, angle);
		superBuffer.light(msLocal.peek().getModel())
			.renderInto(ms, buffers.getBuffer(RenderType.getCutoutMipped()));
	}

	public static void transformHead(MatrixStack ms, float angle) {
		float originOffset = 1 / 16f;
		Vec3d offset = new Vec3d(0, -2 * originOffset, -originOffset).add(VecHelper.getCenterOf(BlockPos.ZERO));
		MatrixStacker.of(ms)
			.translate(offset)
			.rotateX(angle)
			.translateBack(offset);
	}

}
