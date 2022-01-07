package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface FluidPlaceBlockCallback {
	Event<FluidPlaceBlockCallback> EVENT = EventFactory.createArrayBacked(FluidPlaceBlockCallback.class, callbacks -> (world, pos, state) -> {
		for (FluidPlaceBlockCallback callback : callbacks) {
			BlockState newState = callback.onFluidPlaceBlock(world, pos, state);
			if (newState != null) return newState;
		}

		return null;
	});

	BlockState onFluidPlaceBlock(LevelAccessor world, BlockPos pos, BlockState state);
}
