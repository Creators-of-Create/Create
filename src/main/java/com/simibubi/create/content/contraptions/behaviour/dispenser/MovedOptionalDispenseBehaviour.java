package com.simibubi.create.content.contraptions.behaviour.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class MovedOptionalDispenseBehaviour extends MovedDefaultDispenseItemBehaviour {
	protected boolean successful = true;

	@Override
	protected void playDispenseSound(LevelAccessor world, BlockPos pos) {
		world.levelEvent(this.successful ? 1000 : 1001, pos, 0);
	}
}
