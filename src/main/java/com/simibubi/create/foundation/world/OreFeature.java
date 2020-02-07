package com.simibubi.create.foundation.world;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.config.ConfigBase;

import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class OreFeature<T extends IPlacementConfig> extends ConfigBase implements IFeature {

	public String id;

	protected ConfigBool enable;
	protected ConfigInt clusterSize;
	protected ConfigInt minHeight;
	protected ConfigInt maxHeight;

	private Block block;
	private List<Biome> biomeWhitelist;

	public OreFeature(Block block, int clusterSize) {
		this.block = block;
		this.enable = b(true, "enable", "Whether to spawn this in your World");
		this.clusterSize = i(clusterSize, 0, "clusterSize");
		this.minHeight = i(0, 0, "minHeight");
		this.maxHeight = i(256, 0, "maxHeight");
	}

	public OreFeature<T> between(int minHeight, int maxHeight) {
		allValues.remove(this.minHeight);
		allValues.remove(this.maxHeight);
		this.minHeight = i(minHeight, 0, "minHeight");
		this.maxHeight = i(maxHeight, 0, "maxHeight");
		return this;
	}

	public OreFeature<T> inBiomes(Biome... biomes) {
		biomeWhitelist = Arrays.asList(biomes);
		return this;
	}

	public OreFeature<T> inBiomes(Biome.Category category) {
		biomeWhitelist = new LinkedList<>();
		for (Biome biome : ForgeRegistries.BIOMES)
			if (biome.getCategory() == category)
				biomeWhitelist.add(biome);
		return this;
	}

	@Override
	public void onReload() {

	}

	@Override
	public Optional<ConfiguredFeature<?>> createFeature(Biome biome) {
		if (biomeWhitelist != null && !biomeWhitelist.contains(biome))
			return Optional.empty();
		if (!canGenerate()) 
			return Optional.empty();

		Pair<Placement<T>, T> placement = getPlacement();
		ConfiguredFeature<?> createdFeature = Biome.createDecoratedFeature(Feature.ORE,
				new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, block.getDefaultState(),
						clusterSize.get()),
				placement.getKey(), placement.getValue());

		return Optional.of(createdFeature);
	}

	@Override
	public Decoration getGenerationStage() {
		return GenerationStage.Decoration.UNDERGROUND_ORES;
	}

	protected boolean canGenerate() {
		return minHeight.get() < maxHeight.get() && clusterSize.get() > 0 && enable.get()
				&& !AllConfigs.COMMON.worldGen.disable.get();
	}

	protected abstract Pair<Placement<T>, T> getPlacement();

	@Override
	public void addToConfig(Builder builder) {
		registerAll(builder);
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

}
