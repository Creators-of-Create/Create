package com.simibubi.create.content.contraptions.components.actors.dispenser;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class MovedOptionalDispenseBehaviour extends MovedDefaultDispenseItemBehaviour {
	protected boolean successful = true;

	@Override
	protected void playDispenseSound(IWorld world, BlockPos pos) {
		world.playEvent(this.successful ? 1000 : 1001, pos, 0);
	}
}
