package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.level.chunk.HashMapPalette;

@Mixin(HashMapPalette.class)
public interface HashMapPaletteAccessor<T> {
	@Accessor("values")
	CrudeIncrementalIntIdentityHashBiMap<T> create$getValues();
}
