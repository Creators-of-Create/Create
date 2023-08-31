package com.simibubi.create.infrastructure.worldgen;

import static net.minecraft.data.worldgen.features.FeatureUtils.register;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class AllConfiguredFeatures {
	public static final ResourceKey<ConfiguredFeature<?, ?>>
				ZINC_ORE = key("zinc_ore"),
				STRIATED_ORES_OVERWORLD = key("striated_ores_overworld"),
				STRIATED_ORES_NETHER = key("striated_ores_nether");

	private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, Create.asResource(name));
	}

	public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> ctx) {
		RuleTest stoneOreReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
		RuleTest deepslateOreReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

		List<TargetBlockState> zincTargetStates = List.of(
			OreConfiguration.target(stoneOreReplaceables, AllBlocks.ZINC_ORE.get()
				.defaultBlockState()),
			OreConfiguration.target(deepslateOreReplaceables, AllBlocks.DEEPSLATE_ZINC_ORE.get()
				.defaultBlockState())
		);

		register(ctx, ZINC_ORE, Feature.ORE, new OreConfiguration(zincTargetStates, 12));

		List<LayerPattern> overworldLayerPatterns = List.of(
			AllLayerPatterns.SCORIA.get(),
			AllLayerPatterns.CINNABAR.get(),
			AllLayerPatterns.MAGNETITE.get(),
			AllLayerPatterns.MALACHITE.get(),
			AllLayerPatterns.LIMESTONE.get(),
			AllLayerPatterns.OCHRESTONE.get()
		);

		register(ctx, STRIATED_ORES_OVERWORLD, AllFeatures.LAYERED_ORE.get(), new LayeredOreConfiguration(overworldLayerPatterns, 32, 0));

		List<LayerPattern> netherLayerPatterns = List.of(
			AllLayerPatterns.SCORIA_NETHER.get(),
			AllLayerPatterns.SCORCHIA_NETHER.get()
		);

		register(ctx, STRIATED_ORES_NETHER, AllFeatures.LAYERED_ORE.get(), new LayeredOreConfiguration(netherLayerPatterns, 32, 0));
	}
}
