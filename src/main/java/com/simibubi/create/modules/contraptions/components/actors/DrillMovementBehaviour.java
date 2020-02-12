package com.simibubi.create.modules.contraptions.components.actors;

import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementContext;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DrillMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion,
				context.state.get(DrillBlock.FACING).getOpposite());
	}

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(DrillBlock.FACING).getDirectionVec()).scale(.65f);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public SuperByteBuffer renderInContraption(MovementContext context) {
		return DrillTileEntityRenderer.renderInContraption(context);
	}

}
