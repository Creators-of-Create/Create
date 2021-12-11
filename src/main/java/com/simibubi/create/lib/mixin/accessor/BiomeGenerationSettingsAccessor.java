package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;

@Mixin(BiomeGenerationSettings.class)
public interface BiomeGenerationSettingsAccessor {
	@Accessor
	Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> getCarvers();
}
