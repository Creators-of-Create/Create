package com.simibubi.create.modules.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.contraptions.IHaveMovementBehavior.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction.Axis;

public class DrillTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return getRotatingModel(te.getBlockState());
	}

	protected static SuperByteBuffer getRotatingModel(BlockState state) {
		return AllBlockPartials.DRILL.renderOnDirectional(state);
	}

	public static SuperByteBuffer renderInContraption(MovementContext context) {
		BlockState state = context.state;
		SuperByteBuffer buffer = getRotatingModel(state);

		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, state.get(FACING).getOpposite())
				? context.getAnimationSpeed()
				: 0);
		Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (float) (((time * speed) % 360) / 180 * (float) Math.PI);

		return buffer.rotateCentered(axis, angle);
	}

}