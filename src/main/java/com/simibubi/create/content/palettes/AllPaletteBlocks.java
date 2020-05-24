package com.simibubi.create.content.palettes;

import static com.simibubi.create.foundation.data.WindowGen.customWindowBlock;
import static com.simibubi.create.foundation.data.WindowGen.customWindowPane;
import static com.simibubi.create.foundation.data.WindowGen.framedGlass;
import static com.simibubi.create.foundation.data.WindowGen.framedGlassPane;
import static com.simibubi.create.foundation.data.WindowGen.woodenWindowBlock;
import static com.simibubi.create.foundation.data.WindowGen.woodenWindowPane;

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
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.SandBlock;
import net.minecraft.block.WoodType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;

public class AllPaletteBlocks {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
			.itemGroup(() -> Create.palettesCreativeTab)
			.startSection(AllSections.PALETTES);

	// Windows and Glass

	public static final BlockEntry<GlassBlock> TILED_GLASS = REGISTRATE.block("tiled_glass", GlassBlock::new)
		.initialProperties(() -> Blocks.GLASS)
		.addLayer(() -> RenderType::getCutoutMipped)
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final BlockEntry<ConnectedGlassBlock> FRAMED_GLASS =
		framedGlass("framed_glass", new StandardCTBehaviour(AllSpriteShifts.FRAMED_GLASS)),
		HORIZONTAL_FRAMED_GLASS = framedGlass("horizontal_framed_glass",
			new HorizontalCTBehaviour(AllSpriteShifts.HORIZONTAL_FRAMED_GLASS, AllSpriteShifts.FRAMED_GLASS)),
		VERTICAL_FRAMED_GLASS =
			framedGlass("vertical_framed_glass", new HorizontalCTBehaviour(AllSpriteShifts.VERTICAL_FRAMED_GLASS));

	public static final BlockEntry<GlassPaneBlock> TILED_GLASS_PANE =
		WindowGen.standardGlassPane("tiled_glass", Create.asResource("block/palettes/tiled_glass"),
			new ResourceLocation("block/glass_pane_top"), () -> RenderType::getCutoutMipped);

	public static final BlockEntry<ConnectedGlassPaneBlock> FRAMED_GLASS_PANE =
		framedGlassPane("framed_glass", AllSpriteShifts.FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS_PANE = framedGlassPane("horizontal_framed_glass", AllSpriteShifts.HORIZONTAL_FRAMED_GLASS),
		VERTICAL_FRAMED_GLASS_PANE = framedGlassPane("vertical_framed_glass", AllSpriteShifts.VERTICAL_FRAMED_GLASS);

	public static final BlockEntry<WindowBlock> 
		OAK_WINDOW = woodenWindowBlock(WoodType.OAK),
		SPRUCE_WINDOW = woodenWindowBlock(WoodType.SPRUCE),
		BIRCH_WINDOW = woodenWindowBlock(WoodType.BIRCH, () -> RenderType::getTranslucent),
		JUNGLE_WINDOW = woodenWindowBlock(WoodType.JUNGLE), 
		ACACIA_WINDOW = woodenWindowBlock(WoodType.ACACIA),
		DARK_OAK_WINDOW = woodenWindowBlock(WoodType.DARK_OAK), 
		ORNATE_IRON_WINDOW =
			customWindowBlock("ornate_iron_window", AllSpriteShifts.ORNATE_IRON_WINDOW, () -> RenderType::getCutoutMipped);

	public static final BlockEntry<ConnectedGlassPaneBlock> 
		OAK_WINDOW_PANE = woodenWindowPane(WoodType.OAK),
		SPRUCE_WINDOW_PANE = woodenWindowPane(WoodType.SPRUCE),
		BIRCH_WINDOW_PANE = woodenWindowPane(WoodType.BIRCH, () -> RenderType::getTranslucent),
		JUNGLE_WINDOW_PANE = woodenWindowPane(WoodType.JUNGLE), 
		ACACIA_WINDOW_PANE = woodenWindowPane(WoodType.ACACIA),
		DARK_OAK_WINDOW_PANE = woodenWindowPane(WoodType.DARK_OAK), 
		ORNATE_IRON_WINDOW_PANE =
			customWindowPane("ornate_iron_window", AllSpriteShifts.ORNATE_IRON_WINDOW, () -> RenderType::getCutoutMipped);

	// Vanilla stone variant patterns

	public static final PalettesVariantEntry GRANITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.GRANITE, PaletteBlockPatterns.vanillaRange, () -> Blocks.GRANITE);

	public static final PalettesVariantEntry DIORITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DIORITE, PaletteBlockPatterns.vanillaRange, () -> Blocks.DIORITE);

	public static final PalettesVariantEntry ANDESITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.ANDESITE, PaletteBlockPatterns.vanillaRange, () -> Blocks.ANDESITE);

	// Create stone variants

	public static final BlockEntry<SandBlock> LIMESAND = REGISTRATE.block("limesand", p -> new SandBlock(0xD7D7C7, p))
		.initialProperties(() -> Blocks.SAND)
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final BlockEntry<Block> LIMESTONE =
		REGISTRATE.baseBlock("limestone", Block::new, () -> Blocks.SANDSTONE)
			.register();

	public static final PalettesVariantEntry LIMESTONE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.LIMESTONE, PaletteBlockPatterns.standardRange, LIMESTONE);

	public static final BlockEntry<Block> WEATHERED_LIMESTONE =
		REGISTRATE.baseBlock("weathered_limestone", Block::new, () -> Blocks.SANDSTONE)
			.register();

	public static final PalettesVariantEntry WEATHERED_LIMESTONE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.WEATHERED_LIMESTONE, PaletteBlockPatterns.standardRange, WEATHERED_LIMESTONE);

	public static final BlockEntry<Block> DOLOMITE =
		REGISTRATE.baseBlock("dolomite", Block::new, () -> Blocks.QUARTZ_BLOCK)
			.register();

	public static final PalettesVariantEntry DOLOMITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DOLOMITE, PaletteBlockPatterns.standardRange, DOLOMITE);

	public static final BlockEntry<Block> GABBRO = REGISTRATE.baseBlock("gabbro", Block::new, () -> Blocks.ANDESITE)
		.register();

	public static final PalettesVariantEntry GABBRO_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.GABBRO, PaletteBlockPatterns.standardRange, GABBRO);

	public static final BlockEntry<Block> NATURAL_SCORIA = REGISTRATE.block("natural_scoria", Block::new)
		.initialProperties(() -> Blocks.ANDESITE)
		.transform(CreateRegistrate.blockVertexColors(new ScoriaVertexColor()))
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final BlockEntry<Block> SCORIA = REGISTRATE.baseBlock("scoria", Block::new, () -> Blocks.ANDESITE)
		.register();

	public static final PalettesVariantEntry SCORIA_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.SCORIA, PaletteBlockPatterns.standardRange, SCORIA);

	public static final BlockEntry<Block> DARK_SCORIA =
		REGISTRATE.baseBlock("dark_scoria", Block::new, () -> Blocks.ANDESITE)
			.register();

	public static final PalettesVariantEntry DARK_SCORIA_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DARK_SCORIA, PaletteBlockPatterns.standardRange, DARK_SCORIA);

	public static void register() {}

	private static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> palettesCubeAll() {
		return (c, p) -> BlockStateGen.cubeAll(c, p, "palettes/");
	}
}
