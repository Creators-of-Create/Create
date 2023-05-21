package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.Blocks;

public class AllLayerPatterns {

	public static final NonNullSupplier<LayerPattern>

	CINNABAR = () -> LayerPattern.builder()
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.CRIMSITE.getBaseBlock())
			.size(2, 5))
		.layer(l -> l.weight(1)
			.block(Blocks.TUFF)
			.block(Blocks.DEEPSLATE)
			.size(2, 3))
		.layer(l -> l.weight(1)
			.blocks(Blocks.DEEPSLATE, Blocks.TUFF)
			.size(2, 2))
		.layer(l -> l.weight(1)
			.block(AllPaletteStoneTypes.LIMESTONE.getBaseBlock())
			.size(1, 2))
		.build();

	public static final NonNullSupplier<LayerPattern> MAGNETITE = () -> LayerPattern.builder()
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.ASURINE.getBaseBlock())
			.size(2, 5))
		.layer(l -> l.weight(1)
			.block(Blocks.TUFF)
			.block(Blocks.DEEPSLATE)
			.size(2, 3))
		.layer(l -> l.weight(1)
			.blocks(Blocks.DEEPSLATE, Blocks.TUFF)
			.size(2, 2))
		.layer(l -> l.weight(1)
			.block(Blocks.CALCITE)
			.size(1, 2))
		.build();

	public static final NonNullSupplier<LayerPattern> OCHRESTONE = () -> LayerPattern.builder()
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.OCHRUM.getBaseBlock())
			.size(2, 5))
		.layer(l -> l.weight(2)
			.block(Blocks.TUFF)
			.block(Blocks.DEEPSLATE)
			.size(2, 3))
		.layer(l -> l.weight(2)
			.block(Blocks.DRIPSTONE_BLOCK)
			.size(1, 2))
		.build();

	public static final NonNullSupplier<LayerPattern> MALACHITE = () -> LayerPattern.builder()
		.layer(l -> l.weight(4)
			.block(AllPaletteStoneTypes.VERIDIUM.getBaseBlock())
			.size(2, 5))
		.layer(l -> l.weight(2)
			.block(Blocks.TUFF)
			.block(Blocks.ANDESITE)
			.size(2, 3))
		.layer(l -> l.weight(2)
			.blocks(Blocks.TUFF, Blocks.ANDESITE)
			.size(2, 2))
		.layer(l -> l.weight(3)
			.block(Blocks.SMOOTH_BASALT)
			.size(1, 2))
		.build();

	public static final NonNullSupplier<LayerPattern> SCORIA = () -> LayerPattern.builder()
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.SCORIA.getBaseBlock())
			.size(2, 5))
		.layer(l -> l.weight(2)
			.block(Blocks.TUFF)
			.block(Blocks.ANDESITE)
			.size(2, 3))
		.layer(l -> l.weight(1)
			.blocks(Blocks.TUFF, Blocks.ANDESITE)
			.size(2, 2))
		.layer(l -> l.weight(1)
			.block(Blocks.DIORITE)
			.size(1, 2))
		.build();

	public static final NonNullSupplier<LayerPattern> LIMESTONE = () -> LayerPattern.builder()
		.layer(l -> l.weight(1)
			.passiveBlock())
		.layer(l -> l.weight(2)
			.block(Blocks.CALCITE))
		.layer(l -> l.weight(1)
			.block(Blocks.DIORITE))
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.LIMESTONE.getBaseBlock())
			.size(2, 4))
		.build();

	public static final NonNullSupplier<LayerPattern> SCORIA_NETHER = () -> LayerPattern.builder()
		.inNether()
		.layer(l -> l.weight(1)
			.passiveBlock())
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.SCORIA.getBaseBlock())
			.size(2, 5))
		.layer(l -> l.weight(1)
			.block(Blocks.BLACKSTONE)
			.size(1, 2))
		.layer(l -> l.weight(1)
			.block(Blocks.BASALT)
			.block(Blocks.SMOOTH_BASALT)
			.size(2, 3))
		.build();

	public static final NonNullSupplier<LayerPattern> SCORCHIA_NETHER = () -> LayerPattern.builder()
		.inNether()
		.layer(l -> l.weight(4)
			.passiveBlock())
		.layer(l -> l.weight(8)
			.block(AllPaletteStoneTypes.SCORCHIA.getBaseBlock())
			.size(2, 5))
		.layer(l -> l.weight(4)
			.block(Blocks.SOUL_SOIL)
			.block(Blocks.SOUL_SAND)
			.size(1, 3))
		.layer(l -> l.weight(1)
			.block(Blocks.MAGMA_BLOCK))
		.layer(l -> l.weight(4)
			.block(Blocks.BASALT)
			.block(Blocks.SMOOTH_BASALT)
			.size(2, 3))
		.build();

}
