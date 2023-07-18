package com.simibubi.create.content.decoration.palettes;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import java.util.Arrays;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.client.model.generators.ModelFile;

public abstract class PaletteBlockPartial<B extends Block> {

	public static final PaletteBlockPartial<StairBlock> STAIR = new Stairs();
	public static final PaletteBlockPartial<SlabBlock> SLAB = new Slab(false);
	public static final PaletteBlockPartial<SlabBlock> UNIQUE_SLAB = new Slab(true);
	public static final PaletteBlockPartial<WallBlock> WALL = new Wall();

	public static final PaletteBlockPartial<?>[] ALL_PARTIALS = { STAIR, SLAB, WALL };
	public static final PaletteBlockPartial<?>[] FOR_POLISHED = { STAIR, UNIQUE_SLAB, WALL };

	private String name;

	private PaletteBlockPartial(String name) {
		this.name = name;
	}

	public @NonnullType BlockBuilder<B, CreateRegistrate> create(String variantName, PaletteBlockPattern pattern,
		BlockEntry<? extends Block> block, AllPaletteStoneTypes variant) {
		String patternName = Lang.nonPluralId(pattern.createName(variantName));
		String blockName = patternName + "_" + this.name;

		BlockBuilder<B, CreateRegistrate> blockBuilder = Create.registrate()
			.block(blockName, p -> createBlock(block))
			.blockstate((c, p) -> generateBlockState(c, p, variantName, pattern, block))
			.recipe((c, p) -> createRecipes(variant, block, c, p))
			.transform(b -> transformBlock(b, variantName, pattern));

		ItemBuilder<BlockItem, BlockBuilder<B, CreateRegistrate>> itemBuilder = blockBuilder.item()
			.transform(b -> transformItem(b, variantName, pattern));

		if (canRecycle())
			itemBuilder.tag(variant.materialTag);

		return itemBuilder.build();
	}

	protected ResourceLocation getTexture(String variantName, PaletteBlockPattern pattern, int index) {
		return PaletteBlockPattern.toLocation(variantName, pattern.getTexture(index));
	}

	protected BlockBuilder<B, CreateRegistrate> transformBlock(BlockBuilder<B, CreateRegistrate> builder,
		String variantName, PaletteBlockPattern pattern) {
		getBlockTags().forEach(builder::tag);
		return builder.transform(pickaxeOnly());
	}

	protected ItemBuilder<BlockItem, BlockBuilder<B, CreateRegistrate>> transformItem(
		ItemBuilder<BlockItem, BlockBuilder<B, CreateRegistrate>> builder, String variantName,
		PaletteBlockPattern pattern) {
		getItemTags().forEach(builder::tag);
		return builder;
	}

	protected boolean canRecycle() {
		return true;
	}

	protected abstract Iterable<TagKey<Block>> getBlockTags();

	protected abstract Iterable<TagKey<Item>> getItemTags();

	protected abstract B createBlock(Supplier<? extends Block> block);

	protected abstract void createRecipes(AllPaletteStoneTypes type, BlockEntry<? extends Block> patternBlock,
		DataGenContext<Block, ? extends Block> c, RegistrateRecipeProvider p);

	protected abstract void generateBlockState(DataGenContext<Block, B> ctx, RegistrateBlockstateProvider prov,
		String variantName, PaletteBlockPattern pattern, Supplier<? extends Block> block);

	private static class Stairs extends PaletteBlockPartial<StairBlock> {

		public Stairs() {
			super("stairs");
		}

		@Override
		protected StairBlock createBlock(Supplier<? extends Block> block) {
			return new StairBlock(() -> block.get()
				.defaultBlockState(), Properties.copy(block.get()));
		}

		@Override
		protected void generateBlockState(DataGenContext<Block, StairBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPattern pattern, Supplier<? extends Block> block) {
			prov.stairsBlock(ctx.get(), getTexture(variantName, pattern, 0));
		}

		@Override
		protected Iterable<TagKey<Block>> getBlockTags() {
			return Arrays.asList(BlockTags.STAIRS);
		}

		@Override
		protected Iterable<TagKey<Item>> getItemTags() {
			return Arrays.asList(ItemTags.STAIRS);
		}

		@Override
		protected void createRecipes(AllPaletteStoneTypes type, BlockEntry<? extends Block> patternBlock,
			DataGenContext<Block, ? extends Block> c, RegistrateRecipeProvider p) {
			p.stairs(DataIngredient.items(patternBlock), c::get, c.getName(), false);
			p.stonecutting(DataIngredient.tag(type.materialTag), c::get, 1);
		}

	}

	private static class Slab extends PaletteBlockPartial<SlabBlock> {

		private boolean customSide;

		public Slab(boolean customSide) {
			super("slab");
			this.customSide = customSide;
		}

		@Override
		protected SlabBlock createBlock(Supplier<? extends Block> block) {
			return new SlabBlock(Properties.copy(block.get()));
		}

		@Override
		protected boolean canRecycle() {
			return false;
		}

		@Override
		protected void generateBlockState(DataGenContext<Block, SlabBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPattern pattern, Supplier<? extends Block> block) {
			String name = ctx.getName();
			ResourceLocation mainTexture = getTexture(variantName, pattern, 0);
			ResourceLocation sideTexture = customSide ? getTexture(variantName, pattern, 1) : mainTexture;

			ModelFile bottom = prov.models()
				.slab(name, sideTexture, mainTexture, mainTexture);
			ModelFile top = prov.models()
				.slabTop(name + "_top", sideTexture, mainTexture, mainTexture);
			ModelFile doubleSlab;

			if (customSide) {
				doubleSlab = prov.models()
					.cubeColumn(name + "_double", sideTexture, mainTexture);
			} else {
				doubleSlab = prov.models()
					.getExistingFile(prov.modLoc(pattern.createName(variantName)));
			}

			prov.slabBlock(ctx.get(), bottom, top, doubleSlab);
		}

		@Override
		protected Iterable<TagKey<Block>> getBlockTags() {
			return Arrays.asList(BlockTags.SLABS);
		}

		@Override
		protected Iterable<TagKey<Item>> getItemTags() {
			return Arrays.asList(ItemTags.SLABS);
		}

		@Override
		protected void createRecipes(AllPaletteStoneTypes type, BlockEntry<? extends Block> patternBlock,
			DataGenContext<Block, ? extends Block> c, RegistrateRecipeProvider p) {
			p.slab(DataIngredient.items(patternBlock), c::get, c.getName(), false);
			p.stonecutting(DataIngredient.tag(type.materialTag), c::get, 2);
			DataIngredient ingredient = DataIngredient.items(c.get());
			ShapelessRecipeBuilder.shapeless(patternBlock.get())
				.requires(ingredient)
				.requires(ingredient)
				.unlockedBy("has_" + c.getName(), ingredient.getCritereon(p))
				.save(p, Create.ID + ":" + c.getName() + "_recycling");
		}

		@Override
		protected BlockBuilder<SlabBlock, CreateRegistrate> transformBlock(
			BlockBuilder<SlabBlock, CreateRegistrate> builder, String variantName, PaletteBlockPattern pattern) {
			builder.loot((lt, block) -> lt.add(block, RegistrateBlockLootTables.createSlabItemTable(block)));
			return super.transformBlock(builder, variantName, pattern);
		}

	}

	private static class Wall extends PaletteBlockPartial<WallBlock> {

		public Wall() {
			super("wall");
		}

		@Override
		protected WallBlock createBlock(Supplier<? extends Block> block) {
			return new WallBlock(Properties.copy(block.get()));
		}

		@Override
		protected ItemBuilder<BlockItem, BlockBuilder<WallBlock, CreateRegistrate>> transformItem(
			ItemBuilder<BlockItem, BlockBuilder<WallBlock, CreateRegistrate>> builder, String variantName,
			PaletteBlockPattern pattern) {
			builder.model((c, p) -> p.wallInventory(c.getName(), getTexture(variantName, pattern, 0)));
			return super.transformItem(builder, variantName, pattern);
		}

		@Override
		protected void generateBlockState(DataGenContext<Block, WallBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPattern pattern, Supplier<? extends Block> block) {
			prov.wallBlock(ctx.get(), pattern.createName(variantName), getTexture(variantName, pattern, 0));
		}

		@Override
		protected Iterable<TagKey<Block>> getBlockTags() {
			return Arrays.asList(BlockTags.WALLS);
		}

		@Override
		protected Iterable<TagKey<Item>> getItemTags() {
			return Arrays.asList(ItemTags.WALLS);
		}

		@Override
		protected void createRecipes(AllPaletteStoneTypes type, BlockEntry<? extends Block> patternBlock,
			DataGenContext<Block, ? extends Block> c, RegistrateRecipeProvider p) {
			p.stonecutting(DataIngredient.tag(type.materialTag), c::get, 1);
			DataIngredient ingredient = DataIngredient.items(patternBlock);
			ShapedRecipeBuilder.shaped(c.get(), 6)
				.pattern("XXX")
				.pattern("XXX")
				.define('X', ingredient)
				.unlockedBy("has_" + p.safeName(ingredient), ingredient.getCritereon(p))
				.save(p, p.safeId(c.get()));
		}

	}

}
