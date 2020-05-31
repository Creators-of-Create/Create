package com.simibubi.create.content.palettes;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public enum PaletteStoneVariants {

	GRANITE(() -> () -> Blocks.GRANITE),
	DIORITE(() -> () -> Blocks.DIORITE),
	ANDESITE(() -> () -> Blocks.ANDESITE),
	LIMESTONE(() -> AllPaletteBlocks.LIMESTONE),
	WEATHERED_LIMESTONE(() -> AllPaletteBlocks.WEATHERED_LIMESTONE),
	DOLOMITE(() -> AllPaletteBlocks.DOLOMITE),
	GABBRO(() -> AllPaletteBlocks.GABBRO),
	SCORIA(() -> AllPaletteBlocks.SCORIA),
	DARK_SCORIA(() -> AllPaletteBlocks.DARK_SCORIA)

	;

	private Supplier<Supplier<Block>> baseBlock;

	private PaletteStoneVariants(Supplier<Supplier<Block>> baseBlock) {
		this.baseBlock = baseBlock;
	}

	public Supplier<Block> getBaseBlock() {
		return baseBlock.get();
	}

}
