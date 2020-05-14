package com.simibubi.create.modules.palettes;

import static com.simibubi.create.foundation.utility.data.WindowGen.customWindowBlock;
import static com.simibubi.create.foundation.utility.data.WindowGen.customWindowPane;
import static com.simibubi.create.foundation.utility.data.WindowGen.framedGlass;
import static com.simibubi.create.foundation.utility.data.WindowGen.framedGlassPane;
import static com.simibubi.create.foundation.utility.data.WindowGen.woodenWindowBlock;
import static com.simibubi.create.foundation.utility.data.WindowGen.woodenWindowPane;

import com.simibubi.create.AllCTs;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;
import com.simibubi.create.foundation.utility.data.BlockStateGen;
import com.simibubi.create.foundation.utility.data.WindowGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.SandBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;

public class AllPaletteBlocks {

	private static final PalettesRegistrate REGISTRATE = Create.palettesRegistrate();

	// Windows and Glass

	public static final BlockEntry<GlassBlock> TILED_GLASS = REGISTRATE.block("tiled_glass", GlassBlock::new)
		.initialProperties(() -> Blocks.GLASS)
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final BlockEntry<ConnectedGlassBlock> FRAMED_GLASS =
		framedGlass("framed_glass", new StandardCTBehaviour(AllCTs.FRAMED_GLASS.get())),
		HORIZONTAL_FRAMED_GLASS = framedGlass("horizontal_framed_glass",
			new HorizontalCTBehaviour(AllCTs.HORIZONTAL_FRAMED_GLASS.get(), AllCTs.FRAMED_GLASS.get())),
		VERTICAL_FRAMED_GLASS =
			framedGlass("vertical_framed_glass", new HorizontalCTBehaviour(AllCTs.VERTICAL_FRAMED_GLASS.get()));

	public static final BlockEntry<GlassPaneBlock> TILED_GLASS_PANE =
		WindowGen.standardGlassPane("tiled_glass", Create.asResource("block/palettes/tiled_glass"),
			new ResourceLocation("block/glass_pane_top"), () -> RenderType::getCutoutMipped);

	public static final BlockEntry<ConnectedGlassPaneBlock> FRAMED_GLASS_PANE =
		framedGlassPane("framed_glass", AllCTs.FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS_PANE = framedGlassPane("horizontal_framed_glass", AllCTs.HORIZONTAL_FRAMED_GLASS),
		VERTICAL_FRAMED_GLASS_PANE = framedGlassPane("vertical_framed_glass", AllCTs.VERTICAL_FRAMED_GLASS);

	public static final BlockEntry<WindowBlock> OAK_WINDOW = woodenWindowBlock("oak", AllCTs.OAK_WINDOW),
		SPRUCE_WINDOW = woodenWindowBlock("spruce", AllCTs.SPRUCE_WINDOW),
		BIRCH_WINDOW = woodenWindowBlock("birch", AllCTs.BIRCH_WINDOW, () -> RenderType::getTranslucent),
		JUNGLE_WINDOW = woodenWindowBlock("jungle", AllCTs.JUNGLE_WINDOW),
		ACACIA_WINDOW = woodenWindowBlock("acacia", AllCTs.ACACIA_WINDOW),
		DARK_OAK_WINDOW = woodenWindowBlock("dark_oak", AllCTs.DARK_OAK_WINDOW), ORNATE_IRON_WINDOW =
			customWindowBlock("ornate_iron_window", AllCTs.ORNATE_IRON_WINDOW, () -> RenderType::getCutoutMipped);

	public static final BlockEntry<ConnectedGlassPaneBlock> OAK_WINDOW_PANE =
		woodenWindowPane("oak", AllCTs.OAK_WINDOW),
		SPRUCE_WINDOW_PANE = woodenWindowPane("spruce", AllCTs.SPRUCE_WINDOW),
		BIRCH_WINDOW_PANE = woodenWindowPane("birch", AllCTs.BIRCH_WINDOW, () -> RenderType::getTranslucent),
		JUNGLE_WINDOW_PANE = woodenWindowPane("jungle", AllCTs.JUNGLE_WINDOW),
		ACACIA_WINDOW_PANE = woodenWindowPane("acacia", AllCTs.ACACIA_WINDOW),
		DARK_OAK_WINDOW_PANE = woodenWindowPane("dark_oak", AllCTs.DARK_OAK_WINDOW), ORNATE_IRON_WINDOW_PANE =
			customWindowPane("ornate_iron_window", AllCTs.ORNATE_IRON_WINDOW, () -> RenderType::getCutoutMipped);

	// Vanilla stone variant patterns

	public static final PalettesVariantEntry GRANITE_VARIANTS = new PalettesVariantEntry("granite",
		PaletteBlockPatterns.vanillaRange, b -> b.initialProperties(() -> Blocks.GRANITE)
			.simpleItem()
			.register());

	public static final PalettesVariantEntry DIORITE_VARIANTS = new PalettesVariantEntry("diorite",
		PaletteBlockPatterns.vanillaRange, b -> b.initialProperties(() -> Blocks.DIORITE)
			.simpleItem()
			.register());

	public static final PalettesVariantEntry ANDESITE_VARIANTS = new PalettesVariantEntry("andesite",
		PaletteBlockPatterns.vanillaRange, b -> b.initialProperties(() -> Blocks.ANDESITE)
			.simpleItem()
			.register());

	// Create stone variants

	public static final BlockEntry<SandBlock> LIMESAND = REGISTRATE.block("limesand", p -> new SandBlock(0xD7D7C7, p))
		.initialProperties(() -> Blocks.SAND)
		.blockstate(palettesCubeAll())
		.register();

	public static final BlockEntry<Block> LIMESTONE =
		REGISTRATE.baseBlock("limestone", Block::new, () -> Blocks.SANDSTONE)
			.register();

	public static final PalettesVariantEntry LIMESTONE_VARIANTS = new PalettesVariantEntry("limestone",
		PaletteBlockPatterns.standardRange, b -> b.initialProperties(LIMESTONE)
			.simpleItem()
			.register());

	public static final BlockEntry<Block> WEATHERED_LIMESTONE =
		REGISTRATE.baseBlock("weathered_limestone", Block::new, () -> Blocks.SANDSTONE)
			.register();

	public static final PalettesVariantEntry WEATHERED_LIMESTONE_VARIANTS =
		new PalettesVariantEntry("weathered_limestone", PaletteBlockPatterns.standardRange,
			b -> b.initialProperties(WEATHERED_LIMESTONE)
				.simpleItem()
				.register());

	public static final BlockEntry<Block> DOLOMITE =
		REGISTRATE.baseBlock("dolomite", Block::new, () -> Blocks.QUARTZ_BLOCK)
			.register();

	public static final PalettesVariantEntry DOLOMITE_VARIANTS = new PalettesVariantEntry("dolomite",
		PaletteBlockPatterns.standardRange, b -> b.initialProperties(DOLOMITE)
			.simpleItem()
			.register());

	public static final BlockEntry<Block> GABBRO = REGISTRATE.baseBlock("gabbro", Block::new, () -> Blocks.ANDESITE)
		.register();

	public static final PalettesVariantEntry GABBRO_VARIANTS = new PalettesVariantEntry("gabbro",
		PaletteBlockPatterns.standardRange, b -> b.initialProperties(GABBRO)
			.simpleItem()
			.register());

	public static final BlockEntry<ScoriaBlock> NATURAL_SCORIA = REGISTRATE.block("natural_scoria", ScoriaBlock::new)
		.initialProperties(() -> Blocks.ANDESITE)
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final BlockEntry<Block> SCORIA = REGISTRATE.baseBlock("scoria", Block::new, () -> Blocks.ANDESITE)
		.register();

	public static final PalettesVariantEntry SCORIA_VARIANTS = new PalettesVariantEntry("scoria",
		PaletteBlockPatterns.standardRange, b -> b.initialProperties(SCORIA)
			.simpleItem()
			.register());

	public static final BlockEntry<Block> DARK_SCORIA =
		REGISTRATE.baseBlock("dark_scoria", Block::new, () -> Blocks.ANDESITE)
			.register();

	public static final PalettesVariantEntry DARK_SCORIA_VARIANTS = new PalettesVariantEntry("dark_scoria",
		PaletteBlockPatterns.standardRange, b -> b.initialProperties(DARK_SCORIA)
			.simpleItem()
			.register());

	public static void register() {}

	private static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> palettesCubeAll() {
		return (c, p) -> BlockStateGen.cubeAll(c, p, "palettes/");
	}
}
