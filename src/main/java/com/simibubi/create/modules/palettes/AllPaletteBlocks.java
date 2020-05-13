package com.simibubi.create.modules.palettes;

import com.simibubi.create.AllCTs;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;
import com.simibubi.create.foundation.utility.data.BlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SandBlock;

public class AllPaletteBlocks {

	private static final PalettesRegistrate REGISTRATE = Create.palettesRegistrate();

	// Windows and Glass

	public static final BlockEntry<ConnectedGlassBlock> FRAMED_GLASS =
		REGISTRATE.framedGlass("framed_glass", new StandardCTBehaviour(AllCTs.FRAMED_GLASS.get()));

	public static final BlockEntry<ConnectedGlassBlock> HORIZONTAL_FRAMED_GLASS =
		REGISTRATE.framedGlass("horizontal_framed_glass",
			new HorizontalCTBehaviour(AllCTs.HORIZONTAL_FRAMED_GLASS.get(), AllCTs.FRAMED_GLASS.get()));

	public static final BlockEntry<ConnectedGlassBlock> VERTICAL_FRAMED_GLASS =
		REGISTRATE.framedGlass("vertical_framed_glass", new HorizontalCTBehaviour(AllCTs.VERTICAL_FRAMED_GLASS.get()));

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

	public static final BlockEntry<SandBlock> LIMESAND =
		REGISTRATE.createBlock("limesand", p -> new SandBlock(0xD7D7C7, p))
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

	public static final BlockEntry<ScoriaBlock> NATURAL_SCORIA =
		REGISTRATE.createBlock("natural_scoria", ScoriaBlock::new)
			.initialProperties(() -> Blocks.ANDESITE)
			.blockstate(palettesCubeAll())
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
