package com.simibubi.create.foundation.mixin.compat.xaeros;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class, remap = false)
public interface XaeroFullscreenMapAccessor {
	@Accessor("cameraX")
	double create$getCameraX();

	@Accessor("cameraZ")
	double create$getCameraZ();

	@Accessor("scale")
	double create$getScale();

	@Accessor("mapProcessor")
	MapProcessor create$getMapProcessor();
}
