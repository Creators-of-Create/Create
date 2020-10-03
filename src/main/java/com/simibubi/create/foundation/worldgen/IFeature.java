package com.simibubi.create.foundation.worldgen;

import java.util.Optional;

import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public interface IFeature {
	
	public void setId(String id);

	public void addToConfig(ForgeConfigSpec.Builder builder);
	
	public Optional<ConfiguredFeature<?, ?>> createFeature(BiomeLoadingEvent biome);
	
	public Decoration getGenerationStage();

}