package com.simibubi.create.impl.effect;

import java.util.List;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.fluids.FluidStack;

public class WaterEffectHandler implements OpenPipeEffectHandler {
	@Override
	public void apply(Level level, AABB area, FluidStack fluid) {
		if (level.getGameTime() % 5 != 0)
			return;

		List<Entity> entities = level.getEntities((Entity) null, area, Entity::isOnFire);
		for (Entity entity : entities)
			entity.clearFire();

		BlockPos.betweenClosedStream(area).forEach(pos -> dowseFire(level, pos));
	}

	// Adapted from ThrownPotion
	private static void dowseFire(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.is(BlockTags.FIRE)) {
			level.removeBlock(pos, false);
		} else if (AbstractCandleBlock.isLit(state)) {
			AbstractCandleBlock.extinguish(null, state, level, pos);
		} else if (CampfireBlock.isLitCampfire(state)) {
			level.levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
			CampfireBlock.dowse(null, level, pos, state);
			level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
		}
	}
}
