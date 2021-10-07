package com.simibubi.create.content.palettes;

import static com.simibubi.create.content.palettes.PaletteBlockPartial.ALL_PARTIALS;
import static com.simibubi.create.content.palettes.PaletteBlockPartial.FOR_POLISHED;
import static com.simibubi.create.content.palettes.PaletteBlockPattern.PatternNameType.PREFIX;
import static com.simibubi.create.content.palettes.PaletteBlockPattern.PatternNameType.SUFFIX;
import static com.simibubi.create.content.palettes.PaletteBlockPattern.PatternNameType.WRAP;

import java.util.Optional;
import java.util.function.Function;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.ModelGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;

public class PaletteBlockPattern {

	public static final PaletteBlockPattern

		COBBLESTONE = create("cobblestone", SUFFIX, ALL_PARTIALS)
			.blockTags(Tags.Blocks.COBBLESTONE)
			.itemTags(Tags.Items.COBBLESTONE)
			.addRecipes(v -> (c, p) -> {
				DataIngredient ingredient = DataIngredient.items(c.get());
				Block result = v.getBaseBlock().get();
				CookingRecipeBuilder.smelting(ingredient, result, 0.1f, 200)
					.unlockedBy("has_" + p.safeName(ingredient), ingredient.getCritereon(p))
					.save(p, p.safeId(result));
			}),

		POLISHED = create("polished", PREFIX, FOR_POLISHED)
			.blockTags(Tags.Blocks.STONE)
			.itemTags(Tags.Items.STONE)
			.addRecipes(v -> (c, p) -> {
				DataIngredient ingredient = DataIngredient.items(v.getBaseBlock().get());
				ShapedRecipeBuilder.shaped(c.get(), 4)
					.define('#', ingredient)
					.pattern("##")
					.pattern("##")
					.unlockedBy("has_" + p.safeName(ingredient), ingredient.getCritereon(p))
					.save(p, p.safeId(c.get()));
			}),

		BRICKS = create("bricks", SUFFIX, ALL_PARTIALS), FANCY_BRICKS = create("fancy_bricks", WRAP, ALL_PARTIALS),

		PAVED = create("paved", PREFIX, ALL_PARTIALS).blockStateFactory(p -> p::paved)
			.block(PavedBlock::new)
			.textures("paved", "paved_borderless", "paved_top"),

		LAYERED = create("layered", PREFIX).blockStateFactory(p -> p::cubeColumn)
			.textures("layered", "polished")
			.connectedTextures(v -> new HorizontalCTBehaviour(ct(v, CTs.LAYERED), ct(v, CTs.POLISHED))),

		CHISELED = create("chiseled", PREFIX).blockStateFactory(p -> p::cubeColumn)
			.textures("chiseled", "chiseled_top"),

		PILLAR = create("pillar", SUFFIX).blockStateFactory(p -> p::pillar)
			.block(RotatedPillarBlock::new)
			.textures("pillar", "pillar_end")
			.addRecipes(v -> (c, p) -> {
				DataIngredient ingredient = DataIngredient.items(v.getBaseBlock().get());
				ShapedRecipeBuilder.shaped(c.get(), 2)
					.define('#', ingredient)
					.pattern("#")
					.pattern("#")
					.unlockedBy("has_" + p.safeName(ingredient), ingredient.getCritereon(p))
					.save(p, p.safeId(c.get()));
			}),

		MOSSY = create("mossy", PREFIX).blockStateFactory(p -> p::cubeAllButMossy)
			.textures("bricks", "mossy")
			.useTranslucentLayer()
			.withFoliage(),

		OVERGROWN = create("overgrown", PREFIX).blockStateFactory(p -> p::cubeAllButMossy)
			.textures("bricks", "overgrown")
			.useTranslucentLayer()
			.withFoliage()

	;

	public static final PaletteBlockPattern[] VANILLA_RANGE =
		{ COBBLESTONE, BRICKS, FANCY_BRICKS, PILLAR, PAVED, LAYERED, MOSSY, OVERGROWN };

	public static final PaletteBlockPattern[] STANDARD_RANGE =
		{ COBBLESTONE, POLISHED, BRICKS, FANCY_BRICKS, PILLAR, PAVED, LAYERED, CHISELED, MOSSY, OVERGROWN };

	static final String TEXTURE_LOCATION = "block/palettes/%s/%s";
	static final String OVERLAY_LOCATION = "block/palettes/%s";

	private PatternNameType nameType;
	private String[] textures;
	private String id;
	private boolean isTranslucent;
	private boolean hasFoliage;
	private ITag.INamedTag<Block>[] blockTags;
	private ITag.INamedTag<Item>[] itemTags;
	private Optional<Function<PaletteStoneVariants, ConnectedTextureBehaviour>> ctBehaviour;

	private IPatternBlockStateGenerator blockStateGenerator;
	private NonNullFunction<Properties, ? extends Block> blockFactory;
	private NonNullFunction<PaletteStoneVariants, NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateRecipeProvider>> additionalRecipes;
	private PaletteBlockPartial<? extends Block>[] partials;

	@OnlyIn(Dist.CLIENT)
	private RenderType renderType;

	private static PaletteBlockPattern create(String name, PatternNameType nameType,
		PaletteBlockPartial<?>... partials) {
		PaletteBlockPattern pattern = new PaletteBlockPattern();
		pattern.id = name;
		pattern.ctBehaviour = Optional.empty();
		pattern.nameType = nameType;
		pattern.partials = partials;
		pattern.additionalRecipes = $ -> NonNullBiConsumer.noop();
		pattern.isTranslucent = false;
		pattern.hasFoliage = false;
		pattern.blockFactory = Block::new;
		pattern.textures = new String[] { name };
		pattern.blockStateGenerator = p -> p::cubeAll;
		return pattern;
	}

	public IPatternBlockStateGenerator getBlockStateGenerator() {
		return blockStateGenerator;
	}

	public boolean isTranslucent() {
		return isTranslucent;
	}

	public boolean hasFoliage() {
		return hasFoliage;
	}

	public ITag.INamedTag<Block>[] getBlockTags() {
		return blockTags;
	}

	public ITag.INamedTag<Item>[] getItemTags() {
		return itemTags;
	}

	public NonNullFunction<Properties, ? extends Block> getBlockFactory() {
		return blockFactory;
	}

	public PaletteBlockPartial<? extends Block>[] getPartials() {
		return partials;
	}

	public String getTextureForPartials() {
		return textures[0];
	}

	public void addRecipes(PaletteStoneVariants variant, DataGenContext<Block, ? extends Block> c,
		RegistrateRecipeProvider p) {
		additionalRecipes.apply(variant)
			.accept(c, p);
	}

	public Optional<ConnectedTextureBehaviour> createCTBehaviour(PaletteStoneVariants variant) {
		return ctBehaviour.map(f -> f.apply(variant));
	}

	// Builder

	private PaletteBlockPattern blockStateFactory(IPatternBlockStateGenerator factory) {
		blockStateGenerator = factory;
		return this;
	}

	private PaletteBlockPattern textures(String... textures) {
		this.textures = textures;
		return this;
	}

	private PaletteBlockPattern block(NonNullFunction<Properties, ? extends Block> blockFactory) {
		this.blockFactory = blockFactory;
		return this;
	}

	private PaletteBlockPattern useTranslucentLayer() {
		isTranslucent = true;
		return this;
	}

	private PaletteBlockPattern withFoliage() {
		hasFoliage = true;
		return this;
	}

	@SafeVarargs
	private final PaletteBlockPattern blockTags(ITag.INamedTag<Block>... tags) {
		blockTags = tags;
		return this;
	}

	@SafeVarargs
	private final PaletteBlockPattern itemTags(ITag.INamedTag<Item>... tags) {
		itemTags = tags;
		return this;
	}

	private PaletteBlockPattern connectedTextures(Function<PaletteStoneVariants, ConnectedTextureBehaviour> factory) {
		this.ctBehaviour = Optional.of(factory);
		return this;
	}

	private PaletteBlockPattern addRecipes(
		NonNullFunction<PaletteStoneVariants, NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateRecipeProvider>> func) {
		this.additionalRecipes = func;
		return this;
	}

	// Model generators

	public IBlockStateProvider cubeAll(String variant) {
		ResourceLocation all = toLocation(variant, textures[0]);
		return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
			.cubeAll(createName(variant), all));
	}

	public IBlockStateProvider cubeAllButMossy(String variant) {
		ResourceLocation all = toLocation(variant, textures[0]);
		ResourceLocation overlay = toOverlayLocation(textures[1]);
		return (ctx, prov) -> prov.simpleBlock(ctx.get(), ModelGen.createOvergrown(ctx, prov, all, overlay));
	}

	public IBlockStateProvider cubeBottomTop(String variant) {
		ResourceLocation side = toLocation(variant, textures[0]);
		ResourceLocation bottom = toLocation(variant, textures[1]);
		ResourceLocation top = toLocation(variant, textures[2]);
		return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
			.cubeBottomTop(createName(variant), side, bottom, top));
	}

	public IBlockStateProvider pillar(String variant) {
		ResourceLocation side = toLocation(variant, textures[0]);
		ResourceLocation end = toLocation(variant, textures[1]);
		return (ctx, prov) -> BlockStateGen.axisBlock(ctx, prov, $ -> prov.models()
			.cubeColumn(createName(variant), side, end));
	}

	public IBlockStateProvider cubeColumn(String variant) {
		ResourceLocation side = toLocation(variant, textures[0]);
		ResourceLocation end = toLocation(variant, textures[1]);
		return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
			.cubeColumn(createName(variant), side, end));
	}

	public IBlockStateProvider paved(String variant) {
		ResourceLocation side = toLocation(variant, textures[0]);
		ResourceLocation bottom = toLocation(variant, textures[1]);
		ResourceLocation top = toLocation(variant, textures[2]);
		return (ctx, prov) -> {
			ModelFile cubeBottomTop = prov.models()
				.cubeBottomTop(createName(variant), side, bottom, top);
			ModelFile cubeAll = prov.models()
				.cubeAll(createName(variant) + "_covered", bottom);
			BlockStateGen.pavedBlock(ctx, prov, cubeBottomTop, cubeAll);
		};
	}

	// Utility

	protected String createName(String variant) {
		if (nameType == WRAP) {
			String[] split = id.split("_");
			if (split.length == 2) {
				String formatString = "%s_%s_%s";
				return String.format(formatString, split[0], variant, split[1]);
			}
		}
		String formatString = "%s_%s";
		return nameType == SUFFIX ? String.format(formatString, variant, id) : String.format(formatString, id, variant);
	}

	protected ResourceLocation toLocation(String variant, String texture) {
		return Create.asResource(String.format(TEXTURE_LOCATION, variant, texture));
	}

	protected ResourceLocation toOverlayLocation(String texture) {
		return Create.asResource(String.format(OVERLAY_LOCATION, texture));
	}

	protected static CTSpriteShiftEntry ct(PaletteStoneVariants variant, CTs texture) {
		return AllSpriteShifts.getVariantPattern(variant, texture);
	}

	@FunctionalInterface
	static interface IPatternBlockStateGenerator
		extends Function<PaletteBlockPattern, Function<String, IBlockStateProvider>> {
	}

	@FunctionalInterface
	static interface IBlockStateProvider
		extends NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateBlockstateProvider> {
	}

	enum PatternNameType {
		PREFIX, SUFFIX, WRAP
	}

	// Textures with connectability, used by Spriteshifter

	public enum CTs {

		POLISHED(CTType.OMNIDIRECTIONAL), LAYERED(CTType.HORIZONTAL)

		;

		public CTType type;

		private CTs(CTType type) {
			this.type = type;
		}

	}

}
