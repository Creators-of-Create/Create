package com.simibubi.create.compat.sodium;

import com.simibubi.create.Create;

import net.minecraft.resources.Identifier;

import net.neoforged.bus.api.IEventBus;

/**
 * Fixes the Mechanical Saw's sprite and Factory Gauge's sprite
 */
public class SodiumCompat {
	public static final Identifier SAW_TEXTURE = Create.asResource("block/saw_reversed");
	public static final Identifier FACTORY_PANEL_TEXTURE = Create.asResource("block/factory_panel_connections_animated");

	public static void init(IEventBus modEventBus, IEventBus neoEventBus) {
		// TODO 26.2: Rebind Sodium's sprite activity hook once its 26.2 API stabilizes.
	}
}
