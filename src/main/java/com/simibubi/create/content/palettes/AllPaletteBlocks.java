package com.simibubi.create.content.palettes;

import static com.simibubi.create.foundation.data.WindowGen.customWindowBlock;
import static com.simibubi.create.foundation.data.WindowGen.customWindowPane;
import static com.simibubi.create.foundation.data.WindowGen.framedGlass;
import static com.simibubi.create.foundation.data.WindowGen.framedGlassPane;
import static com.simibubi.create.foundation.data.WindowGen.woodenWindowBlock;
import static com.simibubi.create.foundation.data.WindowGen.woodenWindowPane;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.WindowGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.SandBlock;
import net.minecraft.block.WoodType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class AllPaletteBlocks {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.PALETTES_CREATIVE_TAB)
		.startSection(AllSections.PALETTES);

	// Windows and Glass

	public static final BlockEntry<GlassBlock> TILED_GLASS = REGISTRATE.block("tiled_glass", GlassBlock::new)
		.initialProperties(() -> Blocks.GLASS)
		.addLayer(() -> RenderType::getCutoutMipped)
		.recipe((c, p) -> p.stonecutting(DataIngredient.tag(Tags.Items.GLASS_COLORLESS), c::get))
		.blockstate(palettesCubeAll())
		.tag(Tags.Blocks.GLASS_COLORLESS, BlockTags.IMPERMEABLE)
		.item()
		.tag(Tags.Items.GLASS_COLORLESS)
		.build()
		.register();

	public static final BlockEntry<ConnectedGlassBlock> FRAMED_GLASS =
		framedGlass("framed_glass", new StandardCTBehaviour(AllSpriteShifts.FRAMED_GLASS)),
		HORIZONTAL_FRAMED_GLASS = framedGlass("horizontal_framed_glass",
			new HorizontalCTBehaviour(AllSpriteShifts.HORIZONTAL_FRAMED_GLASS, AllSpriteShifts.FRAMED_GLASS)),
		VERTICAL_FRAMED_GLASS =
			framedGlass("vertical_framed_glass", new HorizontalCTBehaviour(AllSpriteShifts.VERTICAL_FRAMED_GLASS));

	public static final BlockEntry<GlassPaneBlock> TILED_GLASS_PANE =
		WindowGen.standardGlassPane("tiled_glass", TILED_GLASS, Create.asResource("block/palettes/tiled_glass"),
			new ResourceLocation("block/glass_pane_top"), () -> RenderType::getCutoutMipped);

	public static final BlockEntry<ConnectedGlassPaneBlock> FRAMED_GLASS_PANE =
		framedGlassPane("framed_glass", FRAMED_GLASS, AllSpriteShifts.FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS_PANE = framedGlassPane("horizontal_framed_glass", HORIZONTAL_FRAMED_GLASS,
			AllSpriteShifts.HORIZONTAL_FRAMED_GLASS),
		VERTICAL_FRAMED_GLASS_PANE =
			framedGlassPane("vertical_framed_glass", VERTICAL_FRAMED_GLASS, AllSpriteShifts.VERTICAL_FRAMED_GLASS);

	public static final BlockEntry<WindowBlock> OAK_WINDOW = woodenWindowBlock(WoodType.OAK, Blocks.OAK_PLANKS),
		SPRUCE_WINDOW = woodenWindowBlock(WoodType.SPRUCE, Blocks.SPRUCE_PLANKS),
		BIRCH_WINDOW = woodenWindowBlock(WoodType.BIRCH, Blocks.BIRCH_PLANKS, () -> RenderType::getTranslucent),
		JUNGLE_WINDOW = woodenWindowBlock(WoodType.JUNGLE, Blocks.JUNGLE_PLANKS),
		ACACIA_WINDOW = woodenWindowBlock(WoodType.ACACIA, Blocks.ACACIA_PLANKS),
		DARK_OAK_WINDOW = woodenWindowBlock(WoodType.DARK_OAK, Blocks.DARK_OAK_PLANKS),
		CRIMSON_WINDOW = woodenWindowBlock(WoodType.CRIMSON, Blocks.CRIMSON_PLANKS),
		WARPED_WINDOW = woodenWindowBlock(WoodType.WARPED, Blocks.WARPED_PLANKS),
		ORNATE_IRON_WINDOW = customWindowBlock("ornate_iron_window", AllItems.ANDESITE_ALLOY,
			AllSpriteShifts.ORNATE_IRON_WINDOW, () -> RenderType::getCutoutMipped);

	public static final BlockEntry<ConnectedGlassPaneBlock> OAK_WINDOW_PANE =
		woodenWindowPane(WoodType.OAK, OAK_WINDOW),
		SPRUCE_WINDOW_PANE = woodenWindowPane(WoodType.SPRUCE, SPRUCE_WINDOW),
		BIRCH_WINDOW_PANE = woodenWindowPane(WoodType.BIRCH, BIRCH_WINDOW, () -> RenderType::getTranslucent),
		JUNGLE_WINDOW_PANE = woodenWindowPane(WoodType.JUNGLE, JUNGLE_WINDOW),
		ACACIA_WINDOW_PANE = woodenWindowPane(WoodType.ACACIA, ACACIA_WINDOW),
		DARK_OAK_WINDOW_PANE = woodenWindowPane(WoodType.DARK_OAK, DARK_OAK_WINDOW),
		CRIMSON_WINDOW_PANE = woodenWindowPane(WoodType.CRIMSON, CRIMSON_WINDOW),
		WARPED_WINDOW_PANE = woodenWindowPane(WoodType.WARPED, WARPED_WINDOW),
		ORNATE_IRON_WINDOW_PANE = customWindowPane("ornate_iron_window", ORNATE_IRON_WINDOW,
			AllSpriteShifts.ORNATE_IRON_WINDOW, () -> RenderType::getCutoutMipped);

	// Vanilla stone variant patterns

	public static final PalettesVariantEntry GRANITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.GRANITE, PaletteBlockPattern.VANILLA_RANGE, () -> Blocks.GRANITE);

	public static final PalettesVariantEntry DIORITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DIORITE, PaletteBlockPattern.VANILLA_RANGE, () -> Blocks.DIORITE);

	public static final PalettesVariantEntry ANDESITE_VARIANTS = new PalettesVariantEntry(PaletteStoneVariants.ANDESITE,
		PaletteBlockPattern.VANILLA_RANGE, () -> Blocks.ANDESITE);

	// Create stone variants

	public static final BlockEntry<SandBlock> LIMESAND = REGISTRATE.block("limesand", p -> new SandBlock(0xD7D7C7, p))
		.initialProperties(() -> Blocks.SAND)
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final BlockEntry<Block> LIMESTONE =
		REGISTRATE.baseBlock("limestone", Block::new, () -> Blocks.SANDSTONE, true)
			.tag(BlockTags.BASE_STONE_OVERWORLD)
			.loot(cobblestoneLoot(PaletteStoneVariants.LIMESTONE))
			.register();

	public static final PalettesVariantEntry LIMESTONE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.LIMESTONE, PaletteBlockPattern.STANDARD_RANGE, LIMESTONE);

	public static final BlockEntry<Block> WEATHERED_LIMESTONE =
		REGISTRATE.baseBlock("weathered_limestone", Block::new, () -> Blocks.SANDSTONE, true)
			.tag(BlockTags.BASE_STONE_OVERWORLD)
			.loot(cobblestoneLoot(PaletteStoneVariants.WEATHERED_LIMESTONE))
			.register();

	public static final PalettesVariantEntry WEATHERED_LIMESTONE_VARIANTS = new PalettesVariantEntry(
		PaletteStoneVariants.WEATHERED_LIMESTONE, PaletteBlockPattern.STANDARD_RANGE, WEATHERED_LIMESTONE);

	public static final BlockEntry<Block> DOLOMITE =
		REGISTRATE.baseBlock("dolomite", Block::new, () -> Blocks.QUARTZ_BLOCK, true)
			.tag(BlockTags.BASE_STONE_OVERWORLD)
			.loot(cobblestoneLoot(PaletteStoneVariants.DOLOMITE))
			.register();

	public static final PalettesVariantEntry DOLOMITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DOLOMITE, PaletteBlockPattern.STANDARD_RANGE, DOLOMITE);

	public static final BlockEntry<Block> GABBRO =
		REGISTRATE.baseBlock("gabbro", Block::new, () -> Blocks.ANDESITE, true)
			.tag(BlockTags.BASE_STONE_OVERWORLD)
			.loot(cobblestoneLoot(PaletteStoneVariants.GABBRO))
			.register();

	public static final PalettesVariantEntry GABBRO_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.GABBRO, PaletteBlockPattern.STANDARD_RANGE, GABBRO);

	public static final BlockEntry<Block> SCORIA =
		REGISTRATE.baseBlock("scoria", Block::new, () -> Blocks.ANDESITE, true)
			.loot(cobblestoneLoot(PaletteStoneVariants.SCORIA))
			.register();

	public static final BlockEntry<Block> NATURAL_SCORIA = REGISTRATE.block("natural_scoria", Block::new)
		.initialProperties(() -> Blocks.ANDESITE)
		.tag(BlockTags.BASE_STONE_OVERWORLD)
		.onRegister(CreateRegistrate.blockVertexColors(new ScoriaVertexColor()))
		.loot((p, g) -> p.registerLootTable(g, RegistrateBlockLootTables.droppingWithSilkTouch(g, SCORIA.get())))
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final PalettesVariantEntry SCORIA_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.SCORIA, PaletteBlockPattern.STANDARD_RANGE, SCORIA);

	public static final BlockEntry<Block> DARK_SCORIA =
		REGISTRATE.baseBlock("dark_scoria", Block::new, () -> Blocks.ANDESITE, false)
			.loot(cobblestoneLoot(PaletteStoneVariants.DARK_SCORIA))
			.register();

	public static final PalettesVariantEntry DARK_SCORIA_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DARK_SCORIA, PaletteBlockPattern.STANDARD_RANGE, DARK_SCORIA);

	private static <T extends Block> NonNullBiConsumer<RegistrateBlockLootTables, T> cobblestoneLoot(PaletteStoneVariants variant) {
		return (loot, block) -> loot.registerLootTable(block, RegistrateBlockLootTables.droppingWithSilkTouch(block,
			variant.getVariants().registeredBlocks.get(0).get()));
	}

	private static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> palettesCubeAll() {
		return (c, p) -> BlockStateGen.cubeAll(c, p, "palettes/");
	}

	public static void register() {}

}
