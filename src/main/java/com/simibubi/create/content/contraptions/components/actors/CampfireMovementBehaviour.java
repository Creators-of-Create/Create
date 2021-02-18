package com.simibubi.create.content.contraptions.components.actors;

import java.util.Random;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.block.CampfireBlock;
import net.minecraft.particles.ParticleTypes;

public class CampfireMovementBehaviour extends MovementBehaviour {
	@Override
	public boolean renderAsNormalTileEntity() {
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world == null || !context.world.isRemote || context.position == null
			|| !context.state.get(CampfireBlock.LIT))
			return;

		// Mostly copied from CampfireBlock and CampfireTileEntity
		Random random = context.world.rand;
		if (random.nextFloat() < 0.11F) {
			for (int i = 0; i < random.nextInt(2) + 2; ++i) {
				context.world.addOptionalParticle(
					context.state.get(CampfireBlock.SIGNAL_FIRE) ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE
						: ParticleTypes.CAMPFIRE_COSY_SMOKE,
					true, context.position.getX() + random.nextDouble() / (random.nextBoolean() ? 3D : -3D),
					context.position.getY() + random.nextDouble() + random.nextDouble(),
					context.position.getZ() + random.nextDouble() / (random.nextBoolean() ? 3D : -3D), 0.0D, 0.07D,
					0.0D);
			}
		}
	}
}
