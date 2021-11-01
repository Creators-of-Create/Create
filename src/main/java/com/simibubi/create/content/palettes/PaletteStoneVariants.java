package com.simibubi.create.content.palettes;

import java.util.function.Supplier;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum PaletteStoneVariants {

	GRANITE(() -> () -> Blocks.GRANITE, () -> AllPaletteBlocks.GRANITE_VARIANTS),
	DIORITE(() -> () -> Blocks.DIORITE, () -> AllPaletteBlocks.DIORITE_VARIANTS),
	ANDESITE(() -> () -> Blocks.ANDESITE, () -> AllPaletteBlocks.ANDESITE_VARIANTS),
	LIMESTONE(() -> AllPaletteBlocks.LIMESTONE, () -> AllPaletteBlocks.LIMESTONE_VARIANTS),
	WEATHERED_LIMESTONE(() -> AllPaletteBlocks.WEATHERED_LIMESTONE, () -> AllPaletteBlocks.WEATHERED_LIMESTONE_VARIANTS),
	DOLOMITE(() -> AllPaletteBlocks.DOLOMITE, () -> AllPaletteBlocks.DOLOMITE_VARIANTS),
	GABBRO(() -> AllPaletteBlocks.GABBRO, () -> AllPaletteBlocks.GABBRO_VARIANTS),
	SCORIA(() -> AllPaletteBlocks.SCORIA, () -> AllPaletteBlocks.SCORIA_VARIANTS),
	DARK_SCORIA(() -> AllPaletteBlocks.DARK_SCORIA, () -> AllPaletteBlocks.DARK_SCORIA_VARIANTS)

	;

	private NonNullSupplier<NonNullSupplier<Block>> baseBlock;
	private Supplier<PalettesVariantEntry> variants;

	private PaletteStoneVariants(NonNullSupplier<NonNullSupplier<Block>> baseBlock, Supplier<PalettesVariantEntry> variants) {
		this.baseBlock = baseBlock;
		this.variants = variants;
	}

	public NonNullSupplier<Block> getBaseBlock() {
		return baseBlock.get();
	}

	public PalettesVariantEntry getVariants() {
		return variants.get();
	}

}
