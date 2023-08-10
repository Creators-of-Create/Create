package com.simibubi.create.infrastructure.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.utility.Couple;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class OreFeatureConfigEntry extends ConfigBase {
	public static final Map<ResourceLocation, OreFeatureConfigEntry> ALL = new HashMap<>();

	public static final Codec<OreFeatureConfigEntry> CODEC = ResourceLocation.CODEC
			.comapFlatMap(OreFeatureConfigEntry::read, entry -> entry.id);

	public final ResourceLocation id;
	public final ConfigInt clusterSize;
	public final ConfigFloat frequency;
	public final ConfigInt minHeight;
	public final ConfigInt maxHeight;

	private BiomeExtension biomeExt;
	private DatagenExtension datagenExt;

	public OreFeatureConfigEntry(ResourceLocation id, int clusterSize, float frequency, int minHeight, int maxHeight) {
		this.id = id;

		this.clusterSize = i(clusterSize, 0, "clusterSize");
		this.frequency = f(frequency, 0, 512, "frequency", "Amount of clusters generated per Chunk.",
			"  >1 to spawn multiple.", "  <1 to make it a chance.", "  0 to disable.");
		this.minHeight = i(minHeight, "minHeight");
		this.maxHeight = i(maxHeight, "maxHeight");

		ALL.put(id, this);
	}

	public BiomeExtension biomeExt() {
		if (biomeExt == null) {
			biomeExt = new BiomeExtension();
		}
		return biomeExt;
	}

	@Nullable
	public StandardDatagenExtension standardDatagenExt() {
		if (datagenExt == null) {
			datagenExt = new StandardDatagenExtension();
		}
		if (datagenExt instanceof StandardDatagenExtension standard) {
			return standard;
		}
		return null;
	}

	@Nullable
	public LayeredDatagenExtension layeredDatagenExt() {
		if (datagenExt == null) {
			datagenExt = new LayeredDatagenExtension();
		}
		if (datagenExt instanceof LayeredDatagenExtension layered) {
			return layered;
		}
		return null;
	}

	@Nullable
	public DatagenExtension datagenExt() {
		if (datagenExt != null) {
			return datagenExt;
		}
		return null;
	}

	public void addToConfig(ForgeConfigSpec.Builder builder) {
		registerAll(builder);
	}

	@Override
	public String getName() {
		return id.getPath();
	}

	public static DataResult<OreFeatureConfigEntry> read(ResourceLocation id) {
		OreFeatureConfigEntry entry = ALL.get(id);
		if (entry != null) {
			return DataResult.success(entry);
		} else {
			return DataResult.error("Not a valid OreFeatureConfigEntry: " + id);
		}
	}

	public class BiomeExtension {
		public ResourceLocation placedFeatureLocation = id;
		public Predicate<BiomeLoadingEvent> biomePredicate = e -> false;

		public BiomeExtension feature(ResourceLocation placedFeature) {
			this.placedFeatureLocation = placedFeature;
			return this;
		}

		public BiomeExtension predicate(Predicate<BiomeLoadingEvent> predicate) {
			this.biomePredicate = predicate;
			return this;
		}

		public void modifyBiomes(BiomeLoadingEvent event, Registry<PlacedFeature> registry) {
			if (biomePredicate.test(event)) {
				Optional<Holder<PlacedFeature>> optionalFeature = registry.getHolder(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, placedFeatureLocation));
				if (optionalFeature.isPresent()) {
					event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, optionalFeature.get());
				}
			}
		}

		public OreFeatureConfigEntry parent() {
			return OreFeatureConfigEntry.this;
		}
	}

	public abstract class DatagenExtension {
		public abstract ConfiguredFeature<?, ?> createConfiguredFeature(RegistryAccess registryAccess);

		public PlacedFeature createPlacedFeature(RegistryAccess registryAccess) {
			Registry<ConfiguredFeature<?, ?>> featureRegistry = registryAccess.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
			Holder<ConfiguredFeature<?, ?>> featureHolder = featureRegistry.getOrCreateHolder(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, id));
			return new PlacedFeature(featureHolder, List.of(new ConfigDrivenPlacement(OreFeatureConfigEntry.this)));
		}

		public OreFeatureConfigEntry parent() {
			return OreFeatureConfigEntry.this;
		}
	}

	public class StandardDatagenExtension extends DatagenExtension {
		public NonNullSupplier<? extends Block> block;
		public NonNullSupplier<? extends Block> deepBlock;
		public NonNullSupplier<? extends Block> netherBlock;

		public StandardDatagenExtension withBlock(NonNullSupplier<? extends Block> block) {
			this.block = block;
			this.deepBlock = block;
			return this;
		}

		public StandardDatagenExtension withBlocks(Couple<NonNullSupplier<? extends Block>> blocks) {
			this.block = blocks.getFirst();
			this.deepBlock = blocks.getSecond();
			return this;
		}

		public StandardDatagenExtension withNetherBlock(NonNullSupplier<? extends Block> block) {
			this.netherBlock = block;
			return this;
		}

		@Override
		public ConfiguredFeature<?, ?> createConfiguredFeature(RegistryAccess registryAccess) {
			List<TargetBlockState> targetStates = new ArrayList<>();
			if (block != null)
				targetStates.add(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, block.get()
					.defaultBlockState()));
			if (deepBlock != null)
				targetStates.add(OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, deepBlock.get()
					.defaultBlockState()));
			if (netherBlock != null)
				targetStates.add(OreConfiguration.target(OreFeatures.NETHER_ORE_REPLACEABLES, netherBlock.get()
					.defaultBlockState()));

			ConfigDrivenOreFeatureConfiguration config = new ConfigDrivenOreFeatureConfiguration(OreFeatureConfigEntry.this, 0, targetStates);
			return new ConfiguredFeature<>(AllFeatures.STANDARD_ORE.get(), config);
		}
	}

	public class LayeredDatagenExtension extends DatagenExtension {
		public final List<NonNullSupplier<LayerPattern>> layerPatterns = new ArrayList<>();

		public LayeredDatagenExtension withLayerPattern(NonNullSupplier<LayerPattern> pattern) {
			this.layerPatterns.add(pattern);
			return this;
		}

		@Override
		public ConfiguredFeature<?, ?> createConfiguredFeature(RegistryAccess registryAccess) {
			List<LayerPattern> layerPatterns = this.layerPatterns.stream()
					.map(NonNullSupplier::get)
					.toList();

			ConfigDrivenLayeredOreFeatureConfiguration config = new ConfigDrivenLayeredOreFeatureConfiguration(OreFeatureConfigEntry.this, 0, layerPatterns);
			return new ConfiguredFeature<>(AllFeatures.LAYERED_ORE.get(), config);
		}
	}
}
