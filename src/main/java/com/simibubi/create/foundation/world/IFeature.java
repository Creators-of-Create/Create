package com.simibubi.create.foundation.world;

import java.util.Optional;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.common.ForgeConfigSpec;

public interface IFeature {
	
	public void setId(String id);

	public void addToConfig(ForgeConfigSpec.Builder builder);
	
	public Optional<ConfiguredFeature<?, ?>> createFeature(Biome biome);
	
	public Decoration getGenerationStage();

}