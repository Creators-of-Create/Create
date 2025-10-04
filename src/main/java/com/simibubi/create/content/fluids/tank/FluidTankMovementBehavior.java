package com.simibubi.create.content.fluids.tank;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.world.level.block.entity.BlockEntity;

// The fluid level needs to be ticked to animate smoothly
public class FluidTankMovementBehavior implements MovementBehaviour {
	@Override
	public boolean mustTickWhileDisabled() {
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world.isClientSide) {
			BlockEntity be = context.contraption.getBlockEntityClientSide(context.localPos);
			if (be instanceof FluidTankBlockEntity tank) {
				tank.getFluidLevel().tickChaser();
			}
		}
	}
}
