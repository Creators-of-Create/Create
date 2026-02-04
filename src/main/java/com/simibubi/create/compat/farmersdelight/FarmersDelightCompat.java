package com.simibubi.create.compat.farmersdelight;

import com.simibubi.create.Create;

import net.neoforged.bus.api.IEventBus;

import vectorwing.farmersdelight.FarmersDelight;

public class FarmersDelightCompat {
	public static void init(IEventBus modEventBus) {
		// Yet empty.
		Create.LOGGER.info("Create Farmer's compat loaded! " + FarmersDelight.MODID);
	}
}
