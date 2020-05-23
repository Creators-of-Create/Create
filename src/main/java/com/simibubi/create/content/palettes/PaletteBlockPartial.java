package com.simibubi.create.content.palettes;

import java.util.Arrays;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;

public abstract class PaletteBlockPartial<B extends Block> {

	public static final PaletteBlockPartial<StairsBlock> STAIR = new Stairs();
	public static final PaletteBlockPartial<SlabBlock> SLAB = new Slab(false);
	public static final PaletteBlockPartial<SlabBlock> UNIQUE_SLAB = new Slab(true);
	public static final PaletteBlockPartial<WallBlock> WALL = new Wall();

	public static final PaletteBlockPartial<?>[] AllPartials = { STAIR, SLAB, WALL };
	public static final PaletteBlockPartial<?>[] ForPolished = { STAIR, UNIQUE_SLAB, WALL };

	private String name;

	private PaletteBlockPartial(String name) {
		this.name = name;
	}

	public @NonnullType BlockBuilder<B, CreateRegistrate> create(String variantName, PaletteBlockPatterns pattern,
		Supplier<? extends Block> block) {
		String patternName = pattern.createName(variantName);
		String blockName = patternName + "_" + this.name;
		return Create.registrate()
			.block(blockName, p -> createBlock(block))
			.blockstate((c, p) -> generateData(c, p, variantName, pattern, block))
			.transform(b -> transformBlock(b, variantName, pattern))
			.item()
			.transform(b -> transformItem(b, variantName, pattern))
			.build();
	}

	protected ResourceLocation getMainTexture(String variantName, PaletteBlockPatterns pattern) {
		return pattern.toLocation(variantName, pattern.getTextureForPartials());
	}

	protected BlockBuilder<B, CreateRegistrate> transformBlock(BlockBuilder<B, CreateRegistrate> builder,
		String variantName, PaletteBlockPatterns pattern) {
		getBlockTags().forEach(builder::tag);
		return builder;
	}

	protected ItemBuilder<BlockItem, BlockBuilder<B, CreateRegistrate>> transformItem(
		ItemBuilder<BlockItem, BlockBuilder<B, CreateRegistrate>> builder, String variantName,
		PaletteBlockPatterns pattern) {
		getItemTags().forEach(builder::tag);
		return builder;
	}

	protected abstract Iterable<Tag<Block>> getBlockTags();

	protected abstract Iterable<Tag<Item>> getItemTags();

	protected abstract B createBlock(Supplier<? extends Block> block);

	protected abstract void generateData(DataGenContext<Block, B> ctx, RegistrateBlockstateProvider prov,
		String variantName, PaletteBlockPatterns pattern, Supplier<? extends Block> block);

	private static class Stairs extends PaletteBlockPartial<StairsBlock> {

		public Stairs() {
			super("stairs");
		}

		@Override
		protected StairsBlock createBlock(Supplier<? extends Block> block) {
			return new StairsBlock(() -> block.get()
				.getDefaultState(), Properties.from(block.get()));
		}

		@Override
		protected void generateData(DataGenContext<Block, StairsBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPatterns pattern, Supplier<? extends Block> block) {
			prov.stairsBlock(ctx.get(), getMainTexture(variantName, pattern));
		}

		@Override
		protected Iterable<Tag<Block>> getBlockTags() {
			return Arrays.asList(BlockTags.STAIRS);
		}

		@Override
		protected Iterable<Tag<Item>> getItemTags() {
			return Arrays.asList(ItemTags.STAIRS);
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
			return new SlabBlock(Properties.from(block.get()));
		}

		@Override
		protected void generateData(DataGenContext<Block, SlabBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPatterns pattern, Supplier<? extends Block> block) {
			String name = ctx.getName();
			ResourceLocation mainTexture = getMainTexture(variantName, pattern);
			ResourceLocation sideTexture =
				customSide ? new ResourceLocation(mainTexture.getNamespace(), mainTexture.getPath() + "_slab")
					: mainTexture;

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
					.getExistingFile(prov.modLoc(name.replace("_slab", "")));
			}

			prov.slabBlock(ctx.get(), bottom, top, doubleSlab);
		}

		@Override
		protected Iterable<Tag<Block>> getBlockTags() {
			return Arrays.asList(BlockTags.SLABS);
		}

		@Override
		protected Iterable<Tag<Item>> getItemTags() {
			return Arrays.asList(ItemTags.SLABS);
		}

	}

	private static class Wall extends PaletteBlockPartial<WallBlock> {

		public Wall() {
			super("wall");
		}

		@Override
		protected WallBlock createBlock(Supplier<? extends Block> block) {
			return new WallBlock(Properties.from(block.get()));
		}

		@Override
		protected ItemBuilder<BlockItem, BlockBuilder<WallBlock, CreateRegistrate>> transformItem(
			ItemBuilder<BlockItem, BlockBuilder<WallBlock, CreateRegistrate>> builder, String variantName,
			PaletteBlockPatterns pattern) {
			builder.model((c, p) -> p.wallInventory(c.getName(), getMainTexture(variantName, pattern)));
			return super.transformItem(builder, variantName, pattern);
		}

		@Override
		protected void generateData(DataGenContext<Block, WallBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPatterns pattern, Supplier<? extends Block> block) {
			prov.wallBlock(ctx.get(), pattern.createName(variantName), getMainTexture(variantName, pattern));
		}

		@Override
		protected Iterable<Tag<Block>> getBlockTags() {
			return Arrays.asList(BlockTags.WALLS);
		}

		@Override
		protected Iterable<Tag<Item>> getItemTags() {
			return Arrays.asList(ItemTags.WALLS);
		}

	}

}
