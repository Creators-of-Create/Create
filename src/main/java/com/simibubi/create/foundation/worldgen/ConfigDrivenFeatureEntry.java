package com.simibubi.create.foundation.worldgen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.config.ConfigBase;
import com.sun.jna.StringArray;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.feature.template.BlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.IRuleTestType;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.Tags;

public class ConfigDrivenFeatureEntry extends ConfigBase {
	public abstract static class FilterConfig extends ConfigBase {
		public abstract boolean isBlacklistByDefault();
		public String[] getBlacklistDescritpion() {
			return new String[]{"Whether or not this filter is a blacklist instead of a whitelist"};
		}
		public String[] getListDescription() {
			return new String[]{"The list for this filter"};
		}
		public abstract List<String> getDefaultList();
		public boolean validateEntry(Object e) {
			return e instanceof String;
		}

		public ConfigBool blacklist = b(isBlacklistByDefault(), "blacklist", getBlacklistDescritpion());
		public ForgeConfigSpec.ConfigValue<List<? extends String>> list;

		@Override
		protected void registerAll(ForgeConfigSpec.Builder builder) {
			builder.comment(getListDescription());
			list = builder.defineList("list", this::getDefaultList, this::validateEntry);
			super.registerAll(builder);
		}
	}

	public final String id;
	public final NonNullSupplier<? extends Block> block;

	protected ConfigInt clusterSize;
	protected final Supplier<FilterConfig> biomeFilter;
	protected final Supplier<FilterConfig> blockFilter;
	protected final Supplier<FilterConfig> categoryFilter;
	protected FilterConfig biomes;
	protected FilterConfig blocks;
	protected FilterConfig categories;
	protected ConfigInt minHeight;
	protected ConfigInt maxHeight;
	protected ConfigFloat frequency;

	Optional<ConfiguredFeature<?, ?>> feature = Optional.empty();

	public ConfigDrivenFeatureEntry(String id, NonNullSupplier<? extends Block> block, int clusterSize,
									float frequency,
									Supplier<FilterConfig> biomeFilter,
									Supplier<FilterConfig> blockFilter,
									Supplier<FilterConfig> categoryFilter) {
		this.id = id;
		this.block = block;
		this.clusterSize = i(clusterSize, 0, "clusterSize");
		this.biomeFilter = biomeFilter;
		this.blockFilter = blockFilter;
		this.categoryFilter = categoryFilter;
		this.minHeight = i(0, 0, "minHeight");
		this.maxHeight = i(256, 0, "maxHeight");
		this.frequency = f(frequency, 0, 512, "frequency", "Amount of clusters generated per Chunk.",
			"  >1 to spawn multiple.", "  <1 to make it a chance.", "  0 to disable.");
	}

	public ConfigDrivenFeatureEntry(String id, NonNullSupplier<? extends Block> block, int clusterSize,
									float frequency) {
		this(id, block, clusterSize, frequency, () -> new FilterConfig() {
			@Override
			public boolean isBlacklistByDefault() {
				return true;
			}

			@Override
			public List<String> getDefaultList() {
				return Collections.emptyList();
			}

			@Override
			public String getName() {
				return "biomes";
			}
		}, () -> new FilterConfig() {
			@Override
			public boolean isBlacklistByDefault() {
				return false;
			}

			@Override
			public List<String> getDefaultList() {
				return Collections.singletonList("#" + BlockTags.BASE_STONE_OVERWORLD.getName());
			}

			@Override
			public String getName() {
				return "blocks";
			}
		}, () -> new FilterConfig() {
			@Override
			public boolean isBlacklistByDefault() {
				return true;
			}

			@Override
			public List<String> getDefaultList() {
				return Arrays.asList(Biome.Category.NETHER.getName(), Biome.Category.THEEND.getName());
			}

			@Override
			public String getName() {
				return "categories";
			}
		});
	}

	public ConfigDrivenFeatureEntry between(int minHeight, int maxHeight) {
		allValues.remove(this.minHeight);
		allValues.remove(this.maxHeight);
		this.minHeight = i(minHeight, 0, "minHeight");
		this.maxHeight = i(maxHeight, 0, "maxHeight");
		return this;
	}

	public ConfiguredFeature<?, ?> getFeature() {
		if (!feature.isPresent())
			feature = Optional.of(createFeature());
		return feature.get();
	}

	public boolean blockMatches(ResourceLocation id, Set<ResourceLocation> tags) {
		return blocks.list.get().stream().anyMatch(l -> {
			if (l.startsWith("#")) {
				return tags.contains(new ResourceLocation(l.replace("#", "")));
			}
			return new ResourceLocation(l).equals(id);
		}) != blocks.blacklist.get();
	}

	protected RuleTest createTest() {
		return new BlockMatchRuleTest(Blocks.AIR) {
			@Override
			public boolean test(BlockState state, Random random) {
				return blockMatches(state.getBlock().getRegistryName(), state.getBlock().getTags());
			}
		};
	}

	private ConfiguredFeature<?, ?> createFeature() {
		ConfigDrivenOreFeatureConfig config =
			new ConfigDrivenOreFeatureConfig(createTest(), block.get()
				.defaultBlockState(), id);

		return ConfigDrivenOreFeature.INSTANCE.configured(config)
			.decorated(ConfigDrivenDecorator.INSTANCE.configured(config));
	}

	protected FilterConfig addFilter(ForgeConfigSpec.Builder builder, Supplier<FilterConfig> c, String... comments) {
		FilterConfig config = c.get();
		builder.comment(comments);
		builder.push(config.getName());
		config.registerAll(builder);
		builder.pop();
		return config;
	}

	@Override
	protected void registerAll(ForgeConfigSpec.Builder builder) {
		biomes = addFilter(builder, biomeFilter, "The biomes this ore spawns in");
		blocks = addFilter(builder, blockFilter, "The blocks this ore can generate in, start with # if it is a tag");
		categories = addFilter(builder, categoryFilter, "The biome categories this ore can spawn in", "Options:",
				Arrays.stream(Biome.Category.values()).map(Biome.Category::getName).collect(Collectors.joining(", ")));
		super.registerAll(builder);
	}

	public void addToConfig(ForgeConfigSpec.Builder builder) {
		registerAll(builder);
	}

	@Override
	public String getName() {
		return id;
	}

	public boolean biomeMatches(ResourceLocation name) {
		return biomes.list.get().stream().anyMatch(l -> new ResourceLocation(l).equals(name)) != biomes.blacklist.get();
	}

	public boolean categoryMatches(String name) {
		return categories.list.get().stream().anyMatch(s -> s.equals(name)) != categories.blacklist.get();
	}
}
