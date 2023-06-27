package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class AllBiomeModifiers {
	public static final ResourceKey<BiomeModifier>
				ZINC_ORE = key("zinc_ore"),
				STRIATED_ORES_OVERWORLD = key("striated_ores_overworld"),
				STRIATED_ORES_NETHER = key("striated_ores_nether");

	private static ResourceKey<BiomeModifier> key(String name) {
		return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, Create.asResource(name));
	}

	public static void bootstrap(BootstapContext<BiomeModifier> ctx) {
		HolderGetter<Biome> biomeLookup = ctx.lookup(Registries.BIOME);
		HolderSet<Biome> isOverworld = biomeLookup.getOrThrow(BiomeTags.IS_OVERWORLD);
		HolderSet<Biome> isNether = biomeLookup.getOrThrow(BiomeTags.IS_NETHER);

		HolderGetter<PlacedFeature> featureLookup = ctx.lookup(Registries.PLACED_FEATURE);
		Holder<PlacedFeature> zincOre = featureLookup.getOrThrow(AllPlacedFeatures.ZINC_ORE);
		Holder<PlacedFeature> striatedOresOverworld = featureLookup.getOrThrow(AllPlacedFeatures.STRIATED_ORES_OVERWORLD);
		Holder<PlacedFeature> striatedOresNether = featureLookup.getOrThrow(AllPlacedFeatures.STRIATED_ORES_NETHER);

		ctx.register(ZINC_ORE, addOre(isOverworld, zincOre));
		ctx.register(STRIATED_ORES_OVERWORLD, addOre(isOverworld, striatedOresOverworld));
		ctx.register(STRIATED_ORES_NETHER, addOre(isNether, striatedOresNether));
	}

	private static AddFeaturesBiomeModifier addOre(HolderSet<Biome> biomes, Holder<PlacedFeature> feature) {
		return new AddFeaturesBiomeModifier(biomes, HolderSet.direct(feature), Decoration.UNDERGROUND_ORES);
	}
}
