package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.curiosities.bell.AbstractBellBlock;

import net.minecraft.block.Block;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BellMovementBehaviour extends MovementBehaviour {

	@Override
	public boolean renderAsNormalTileEntity() {
		return true;
	}

	@Override
	public void onSpeedChanged(MovementContext context, Vector3d oldMotion, Vector3d motion) {
		double dotProduct = oldMotion.dotProduct(motion);

		if (dotProduct <= 0 && (context.relativeMotion.length() != 0) || context.firstMovement)
			playSound(context);
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.position != null)
			playSound(context);
	}

	public static void playSound(MovementContext context) {
		World world = context.world;
		BlockPos pos = new BlockPos(context.position);
		Block block = context.state.getBlock();

		if (block instanceof AbstractBellBlock) {
			((AbstractBellBlock) block).playSound(world, pos);
		} else {
			// Vanilla bell sound
			world.playSound(null, pos, SoundEvents.BLOCK_BELL_USE,
					SoundCategory.BLOCKS, 2f, 1f);
		}
	}
}
