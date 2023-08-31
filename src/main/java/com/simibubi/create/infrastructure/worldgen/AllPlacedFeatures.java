package com.simibubi.create.infrastructure.worldgen;

import static net.minecraft.data.worldgen.placement.PlacementUtils.register;

import java.util.List;

import com.simibubi.create.Create;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class AllPlacedFeatures {
	public static final ResourceKey<PlacedFeature>
				ZINC_ORE = key("zinc_ore"),
				STRIATED_ORES_OVERWORLD = key("striated_ores_overworld"),
				STRIATED_ORES_NETHER = key("striated_ores_nether");
	
	private static ResourceKey<PlacedFeature> key(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, Create.asResource(name));
	}

	public static void bootstrap(BootstapContext<PlacedFeature> ctx) {
		HolderGetter<ConfiguredFeature<?, ?>> featureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);
		Holder<ConfiguredFeature<?, ?>> zincOre = featureLookup.getOrThrow(AllConfiguredFeatures.ZINC_ORE);
		Holder<ConfiguredFeature<?, ?>> striatedOresOverworld = featureLookup.getOrThrow(AllConfiguredFeatures.STRIATED_ORES_OVERWORLD);
		Holder<ConfiguredFeature<?, ?>> striatedOresNether = featureLookup.getOrThrow(AllConfiguredFeatures.STRIATED_ORES_NETHER);

		register(ctx, ZINC_ORE, zincOre, placement(CountPlacement.of(8), -63, 70));
		register(ctx, STRIATED_ORES_OVERWORLD, striatedOresOverworld, placement(RarityFilter.onAverageOnceEvery(18), -30, 70));
		register(ctx, STRIATED_ORES_NETHER, striatedOresNether, placement(RarityFilter.onAverageOnceEvery(18), 40, 90));
	}

	private static List<PlacementModifier> placement(PlacementModifier frequency, int minHeight, int maxHeight) {
		return List.of(
				frequency,
				InSquarePlacement.spread(),
				HeightRangePlacement.uniform(VerticalAnchor.absolute(minHeight), VerticalAnchor.absolute(maxHeight)),
				ConfigPlacementFilter.INSTANCE
		);
	}
}
