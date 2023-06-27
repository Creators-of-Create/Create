package com.simibubi.create.content.contraptions.behaviour;

import com.simibubi.create.content.equipment.bell.AbstractBellBlock;
import com.simibubi.create.content.trains.entity.CarriageContraption;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class BellMovementBehaviour implements MovementBehaviour {

	@Override
	public boolean renderAsNormalBlockEntity() {
		return true;
	}
	
	@Override
	public boolean isActive(MovementContext context) {
		return MovementBehaviour.super.isActive(context) && !(context.contraption instanceof CarriageContraption);
	}

	@Override
	public void onSpeedChanged(MovementContext context, Vec3 oldMotion, Vec3 motion) {
		double dotProduct = oldMotion.dot(motion);

		if (dotProduct <= 0 && (context.relativeMotion.length() != 0) || context.firstMovement)
			playSound(context);
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.position != null && isActive(context))
			playSound(context);
	}

	public static void playSound(MovementContext context) {
		Level world = context.world;
		BlockPos pos = new BlockPos(context.position);
		Block block = context.state.getBlock();

		if (block instanceof AbstractBellBlock) {
			((AbstractBellBlock<?>) block).playSound(world, pos);
		} else {
			// Vanilla bell sound
			world.playSound(null, pos, SoundEvents.BELL_BLOCK,
					SoundSource.BLOCKS, 2f, 1f);
		}
	}
}
