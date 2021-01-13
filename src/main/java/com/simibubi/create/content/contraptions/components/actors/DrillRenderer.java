package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.render.instancing.InstanceBuffer;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;

import com.simibubi.create.foundation.render.instancing.RotatingData;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class DrillRenderer extends KineticTileEntityRenderer {

	public DrillRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected InstanceBuffer<RotatingData> getRotatedModel(InstanceContext<? extends KineticTileEntity> ctx) {
		return AllBlockPartials.DRILL_HEAD.renderOnDirectionalSouthRotating(ctx, ctx.te.getBlockState());
	}

	protected static SuperByteBuffer getRotatingModel(BlockState state) {
		return AllBlockPartials.DRILL_HEAD.renderOnDirectionalSouth(state);
	}

	public static void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };
		BlockState state = context.state;
		SuperByteBuffer superBuffer = AllBlockPartials.DRILL_HEAD.renderOn(state);
		Direction facing = state.get(DrillBlock.FACING);
		
		float speed = (float) (context.contraption.stalled
			|| !VecHelper.isVecPointingTowards(context.relativeMotion, state.get(FACING)
				.getOpposite()) ? context.getAnimationSpeed() : 0);
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (float) (((time * speed) % 360));

		for (MatrixStack m : matrixStacks)
			MatrixStacker.of(m)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing))
				.rotateZ(angle)
				.unCentre();
		
		superBuffer
			.light(msLocal.peek()
			.getModel())
			.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

}