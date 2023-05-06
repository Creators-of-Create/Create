package com.simibubi.create.content.contraptions.components.actors;

import java.util.Random;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.CampfireBlock;

public class CampfireMovementBehaviour implements MovementBehaviour {
	@Override
	public boolean renderAsNormalBlockEntity() {
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world == null || !context.world.isClientSide || context.position == null
			|| !context.state.getValue(CampfireBlock.LIT) || context.disabled)
			return;

		// Mostly copied from CampfireBlock and CampfireBlockEntity
		Random random = context.world.random;
		if (random.nextFloat() < 0.11F) {
			for (int i = 0; i < random.nextInt(2) + 2; ++i) {
				context.world.addAlwaysVisibleParticle(
					context.state.getValue(CampfireBlock.SIGNAL_FIRE) ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE
						: ParticleTypes.CAMPFIRE_COSY_SMOKE,
					true, context.position.x() + random.nextDouble() / (random.nextBoolean() ? 3D : -3D),
					context.position.y() + random.nextDouble() + random.nextDouble(),
					context.position.z() + random.nextDouble() / (random.nextBoolean() ? 3D : -3D), 0.0D, 0.07D,
					0.0D);
			}
		}
	}
}
