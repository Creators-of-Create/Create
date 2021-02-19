package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BellMovementBehaviour extends MovementBehaviour {
	@Override
	public boolean renderAsNormalTileEntity() {
		return true;
	}

	@Override
	public void onSpeedChanged(MovementContext context, Vec3d oldMotion, Vec3d motion) {
		double dotProduct = oldMotion.dotProduct(motion);

		if (dotProduct <= 0 && (context.relativeMotion.length() != 0) || context.firstMovement)
			context.world.playSound(null, new BlockPos(context.position), SoundEvents.BLOCK_BELL_USE,
				SoundCategory.BLOCKS, 2.0F, 1.0F);
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.position != null)
			context.world.playSound(null, new BlockPos(context.position), SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS,
				2.0F, 1.0F);
	}
}
