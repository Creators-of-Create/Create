package com.simibubi.create.foundation.block;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.simibubi.create.foundation.mixin.accessor.StairBlockAccessor;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import org.apache.commons.lang3.ArrayUtils;

import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.block.WeatheringCopperSlabBlock;
import net.minecraft.world.level.block.WeatheringCopperStairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelProvider;

public class CopperBlockSet {
	protected static final WeatherState[] WEATHER_STATES = WeatherState.values();
	protected static final int WEATHER_STATE_COUNT = WEATHER_STATES.length;

	protected static final Map<WeatherState, Supplier<Block>> BASE_BLOCKS = new EnumMap<>(WeatherState.class);
	static {
		BASE_BLOCKS.put(WeatherState.UNAFFECTED, Blocks.COPPER_BLOCK.delegate);
		BASE_BLOCKS.put(WeatherState.EXPOSED, Blocks.EXPOSED_COPPER.delegate);
		BASE_BLOCKS.put(WeatherState.WEATHERED, Blocks.WEATHERED_COPPER.delegate);
		BASE_BLOCKS.put(WeatherState.OXIDIZED, Blocks.OXIDIZED_COPPER.delegate);
	}

	public static final Variant<?>[] DEFAULT_VARIANTS =
		new Variant<?>[] { BlockVariant.INSTANCE, SlabVariant.INSTANCE, StairVariant.INSTANCE };

	protected final String name;
	protected final String generalDirectory; // Leave empty for root folder
	protected final Variant<?>[] variants;
	protected final Map<Variant<?>, BlockEntry<?>[]> entries = new HashMap<>();
	protected final NonNullBiConsumer<DataGenContext<Block, ?>, RegistrateRecipeProvider> mainBlockRecipe;
	protected final String endTextureName;

	public CopperBlockSet(AbstractRegistrate<?> registrate, String name, String endTextureName, Variant<?>[] variants) {
		this(registrate, name, endTextureName, variants, NonNullBiConsumer.noop(), "copper/");
	}

	public CopperBlockSet(AbstractRegistrate<?> registrate, String name, String endTextureName, Variant<?>[] variants, String generalDirectory) {
		this(registrate, name, endTextureName, variants, NonNullBiConsumer.noop(), generalDirectory);
	}

	public CopperBlockSet(AbstractRegistrate<?> registrate, String name, String endTextureName, Variant<?>[] variants, NonNullBiConsumer<DataGenContext<Block, ?>, RegistrateRecipeProvider> mainBlockRecipe) {
		this(registrate, name, endTextureName, variants, mainBlockRecipe, "copper/");
	}

	public CopperBlockSet(AbstractRegistrate<?> registrate, String name, String endTextureName, Variant<?>[] variants,
		NonNullBiConsumer<DataGenContext<Block, ?>, RegistrateRecipeProvider> mainBlockRecipe, String generalDirectory) {
		this.name = name;
		this.generalDirectory = generalDirectory;
		this.endTextureName = endTextureName;
		this.variants = variants;
		this.mainBlockRecipe = mainBlockRecipe;
		for (boolean waxed : Iterate.falseAndTrue) {
			for (Variant<?> variant : this.variants) {
				BlockEntry<?>[] entries =
					waxed ? this.entries.get(variant) : new BlockEntry<?>[WEATHER_STATE_COUNT * 2];
				for (WeatherState state : WEATHER_STATES) {
					int index = getIndex(state, waxed);
					BlockEntry<?> entry = createEntry(registrate, variant, state, waxed);
					entries[index] = entry;

					if (waxed) {
						CopperRegistries.addWaxable(() -> entries[getIndex(state, false)].get(), () -> entry.get());
					} else if (state != WeatherState.UNAFFECTED) {
						CopperRegistries.addWeathering(
							() -> entries[getIndex(WEATHER_STATES[state.ordinal() - 1], false)].get(),
							() -> entry.get());
					}
				}
				if (!waxed)
					this.entries.put(variant, entries);
			}
		}
	}

	protected <T extends Block> BlockEntry<?> createEntry(AbstractRegistrate<?> registrate, Variant<T> variant,
		WeatherState state, boolean waxed) {
		String name = "";
		if (waxed) {
			name += "waxed_";
		}
		name += getWeatherStatePrefix(state);
		name += this.name;

		String suffix = variant.getSuffix();
		if (!suffix.equals(""))
			name = Lang.nonPluralId(name);

		name += suffix;

		Supplier<Block> baseBlock = BASE_BLOCKS.get(state);
		BlockBuilder<T, ?> builder = registrate.block(name, variant.getFactory(this, state, waxed))
			.initialProperties(() -> baseBlock.get())
			.loot((lt, block) -> variant.generateLootTable(lt, block, this, state, waxed))
			.blockstate((ctx, prov) -> variant.generateBlockState(ctx, prov, this, state, waxed))
			.recipe((c, p) -> variant.generateRecipes(entries.get(BlockVariant.INSTANCE)[state.ordinal()], c, p))
			.transform(TagGen.pickaxeOnly())
			.tag(BlockTags.NEEDS_STONE_TOOL)
			.simpleItem();

		if (variant == BlockVariant.INSTANCE && state == WeatherState.UNAFFECTED)
			builder.recipe((c, p) -> mainBlockRecipe.accept(c, p));

		if (waxed) {
			builder.recipe((ctx, prov) -> {
				Block unwaxed = get(variant, state, false).get();
				ShapelessRecipeBuilder.shapeless(ctx.get())
					.requires(unwaxed)
					.requires(Items.HONEYCOMB)
					.unlockedBy("has_unwaxed", RegistrateRecipeProvider.has(unwaxed))
					.save(prov, new ResourceLocation(ctx.getId()
						.getNamespace(), "crafting/" + generalDirectory + ctx.getName() + "_from_honeycomb"));
			});
		}

		return builder.register();
	}

	protected int getIndex(WeatherState state, boolean waxed) {
		return state.ordinal() + (waxed ? WEATHER_STATE_COUNT : 0);
	}

	public String getName() {
		return name;
	}

	public String getEndTextureName() {
		return endTextureName;
	}

	public Variant<?>[] getVariants() {
		return variants;
	}

	public boolean hasVariant(Variant<?> variant) {
		return ArrayUtils.contains(variants, variant);
	}

	public BlockEntry<?> get(Variant<?> variant, WeatherState state, boolean waxed) {
		BlockEntry<?>[] entries = this.entries.get(variant);
		if (entries != null) {
			return entries[getIndex(state, waxed)];
		}
		return null;
	}

	public BlockEntry<?> getStandard() {
		return get(BlockVariant.INSTANCE, WeatherState.UNAFFECTED, false);
	}

	public static String getWeatherStatePrefix(WeatherState state) {
		if (state != WeatherState.UNAFFECTED) {
			return state.name()
				.toLowerCase(Locale.ROOT) + "_";
		}
		return "";
	}

	public interface Variant<T extends Block> {
		String getSuffix();

		NonNullFunction<Properties, T> getFactory(CopperBlockSet blocks, WeatherState state, boolean waxed);

		default void generateLootTable(RegistrateBlockLootTables lootTable, T block, CopperBlockSet blocks,
			WeatherState state, boolean waxed) {
			lootTable.dropSelf(block);
		}

		void generateRecipes(BlockEntry<?> blockVariant, DataGenContext<Block, T> ctx, RegistrateRecipeProvider prov);

		void generateBlockState(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, CopperBlockSet blocks,
			WeatherState state, boolean waxed);
	}

	public static class BlockVariant implements Variant<Block> {
		public static final BlockVariant INSTANCE = new BlockVariant();

		protected BlockVariant() {}

		@Override
		public String getSuffix() {
			return "";
		}

		@Override
		public NonNullFunction<Properties, Block> getFactory(CopperBlockSet blocks, WeatherState state, boolean waxed) {
			if (waxed) {
				return Block::new;
			} else {
				return p -> new WeatheringCopperFullBlock(state, p);
			}
		}

		@Override
		public void generateBlockState(DataGenContext<Block, Block> ctx, RegistrateBlockstateProvider prov,
			CopperBlockSet blocks, WeatherState state, boolean waxed) {
			Block block = ctx.get();
			String path = RegisteredObjects.getKeyOrThrow(block)
				.getPath();
			String baseLoc = ModelProvider.BLOCK_FOLDER + "/" + blocks.generalDirectory + getWeatherStatePrefix(state);

			ResourceLocation texture = prov.modLoc(baseLoc + blocks.getName());
			if (Objects.equals(blocks.getName(), blocks.getEndTextureName())) {
				// End texture and base texture are equal, so we should use cube_all.
				prov.simpleBlock(block, prov.models().cubeAll(path, texture));
			} else {
				// End texture and base texture aren't equal, so we should use cube_column.
				ResourceLocation endTexture = prov.modLoc(baseLoc + blocks.getEndTextureName());
				prov.simpleBlock(block, prov.models()
						.cubeColumn(path, texture, endTexture));
			}

		}

		@Override
		public void generateRecipes(BlockEntry<?> blockVariant, DataGenContext<Block, Block> ctx,
			RegistrateRecipeProvider prov) {}

	}

	public static class SlabVariant implements Variant<SlabBlock> {
		public static final SlabVariant INSTANCE = new SlabVariant();

		protected SlabVariant() {}

		@Override
		public String getSuffix() {
			return "_slab";
		}

		@Override
		public NonNullFunction<Properties, SlabBlock> getFactory(CopperBlockSet blocks, WeatherState state,
			boolean waxed) {
			if (waxed) {
				return SlabBlock::new;
			} else {
				return p -> new WeatheringCopperSlabBlock(state, p);
			}
		}

		@Override
		public void generateLootTable(RegistrateBlockLootTables lootTable, SlabBlock block, CopperBlockSet blocks,
			WeatherState state, boolean waxed) {
			lootTable.add(block, RegistrateBlockLootTables.createSlabItemTable(block));
		}

		@Override
		public void generateBlockState(DataGenContext<Block, SlabBlock> ctx, RegistrateBlockstateProvider prov,
			CopperBlockSet blocks, WeatherState state, boolean waxed) {
			ResourceLocation fullModel =
				prov.modLoc(ModelProvider.BLOCK_FOLDER + "/" + getWeatherStatePrefix(state) + blocks.getName());

			String baseLoc = ModelProvider.BLOCK_FOLDER + "/" + blocks.generalDirectory + getWeatherStatePrefix(state);
			ResourceLocation texture = prov.modLoc(baseLoc + blocks.getName());
			ResourceLocation endTexture = prov.modLoc(baseLoc + blocks.getEndTextureName());

			prov.slabBlock(ctx.get(), fullModel, texture, endTexture, endTexture);
		}

		@Override
		public void generateRecipes(BlockEntry<?> blockVariant, DataGenContext<Block, SlabBlock> ctx,
			RegistrateRecipeProvider prov) {
			prov.slab(DataIngredient.items(blockVariant.get()), ctx::get, null, true);
		}
	}

	public static class StairVariant implements Variant<StairBlock> {
		public static final StairVariant INSTANCE = new StairVariant(BlockVariant.INSTANCE);

		protected final Variant<?> parent;

		protected StairVariant(Variant<?> parent) {
			this.parent = parent;
		}

		@Override
		public String getSuffix() {
			return "_stairs";
		}

		@Override
		public NonNullFunction<Properties, StairBlock> getFactory(CopperBlockSet blocks, WeatherState state,
			boolean waxed) {
			if (!blocks.hasVariant(parent)) {
				throw new IllegalStateException(
					"Cannot add StairVariant '" + toString() + "' without parent Variant '" + parent.toString() + "'!");
			}
			Supplier<BlockState> defaultStateSupplier = () -> blocks.get(parent, state, waxed)
				.getDefaultState();
			if (waxed) {
				return p -> new StairBlock(defaultStateSupplier, p);
			} else {
				return p -> {
					WeatheringCopperStairBlock block =
						new WeatheringCopperStairBlock(state, Blocks.AIR.defaultBlockState(), p);
					// WeatheringCopperStairBlock does not have a constructor that takes a Supplier,
					// so setting the field directly is the easiest solution
					//((StairBlockAccessor)block).setStateSupplier(defaultStateSupplier);
					ObfuscationReflectionHelper.setPrivateValue(StairBlock.class, block, defaultStateSupplier, "stateSupplier");
					return block;
				};
			}
		}

		@Override
		public void generateBlockState(DataGenContext<Block, StairBlock> ctx, RegistrateBlockstateProvider prov,
			CopperBlockSet blocks, WeatherState state, boolean waxed) {
			String baseLoc = ModelProvider.BLOCK_FOLDER + "/" + blocks.generalDirectory + getWeatherStatePrefix(state);
			ResourceLocation texture = prov.modLoc(baseLoc + blocks.getName());
			ResourceLocation endTexture = prov.modLoc(baseLoc + blocks.getEndTextureName());
			prov.stairsBlock(ctx.get(), texture, endTexture, endTexture);
		}

		@Override
		public void generateRecipes(BlockEntry<?> blockVariant, DataGenContext<Block, StairBlock> ctx,
			RegistrateRecipeProvider prov) {
			prov.stairs(DataIngredient.items(blockVariant.get()), ctx::get, null, true);
		}
	}
}
