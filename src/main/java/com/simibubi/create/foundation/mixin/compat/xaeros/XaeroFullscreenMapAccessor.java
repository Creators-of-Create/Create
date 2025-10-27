package com.simibubi.create.foundation.mixin.compat.xaeros;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;

@Mixin(GuiMap.class)
public interface XaeroFullscreenMapAccessor {
	@Accessor(remap = false)
	double getCameraX();

	@Accessor(remap = false)
	double getCameraZ();

	@Accessor(remap = false)
	double getScale();

	@Accessor(remap = false)
	MapProcessor getMapProcessor();
}
