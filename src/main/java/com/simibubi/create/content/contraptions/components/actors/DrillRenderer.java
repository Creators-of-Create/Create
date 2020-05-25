package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class DrillRenderer extends KineticTileEntityRenderer {

	public DrillRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return getRotatingModel(te.getBlockState());
	}

	protected static SuperByteBuffer getRotatingModel(BlockState state) {
		return AllBlockPartials.DRILL_HEAD.renderOnDirectional(state);
	}

	public static void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };
		BlockState state = context.state;
		SuperByteBuffer superBuffer = getRotatingModel(state);

		float speed = (float) (context.contraption.stalled
			|| !VecHelper.isVecPointingTowards(context.relativeMotion, state.get(FACING)
				.getOpposite()) ? context.getAnimationSpeed() : 0);
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (float) (((time * speed) % 360) / 180 * (float) Math.PI);

		for (MatrixStack m : matrixStacks)
			MatrixStacker.of(m)
				.centre()
				.rotateY(angle)
				.unCentre();
		superBuffer.light(msLocal.peek()
			.getModel())
			.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

}