package com.simibubi.create.impl.effect;

import java.util.List;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.fluids.FluidStack;

public class LavaEffectHandler implements OpenPipeEffectHandler {
	@Override
	public void apply(Level level, AABB area, FluidStack fluid) {
		if (level.getGameTime() % 5 != 0)
			return;

		List<Entity> entities = level.getEntities((Entity) null, area, entity -> !entity.fireImmune());
		for (Entity entity : entities) {
			entity.igniteForSeconds(3);
		}
	}
}
