package com.simibubi.create.lib.mixin.accessor;

import java.util.List;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

@Mixin(BiomeGenerationSettings.Builder.class)
public interface BuilderAccessor {
	@Mutable
	@Accessor
	void setFeatures(List<List<Supplier<PlacedFeature>>> features);
}
