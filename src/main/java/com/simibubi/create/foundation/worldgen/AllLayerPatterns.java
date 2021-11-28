package com.simibubi.create.foundation.worldgen;

import com.simibubi.create.content.palettes.AllPaletteStoneTypes;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.Blocks;

public class AllLayerPatterns {

	public static NonNullSupplier<LayerPattern>

	CINNABAR = () -> LayerPattern.builder()
		.layer(l -> l.weight(1)
			.passiveBlock())
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.CRIMSITE.getBaseBlock())
			.size(1, 3))
		.layer(l -> l.weight(2)
			.block(Blocks.TUFF)
			.block(Blocks.GRANITE)
			.size(1, 2))
		.layer(l -> l.weight(1)
			.block(AllPaletteStoneTypes.LIMESTONE.getBaseBlock()))
		.build();

	public static NonNullSupplier<LayerPattern> MAGNETITE = () -> LayerPattern.builder()
		.layer(l -> l.weight(1)
			.passiveBlock())
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.ASURINE.getBaseBlock())
			.size(1, 3))
		.layer(l -> l.weight(2)
			.block(Blocks.TUFF)
			.block(Blocks.SMOOTH_BASALT)
			.size(1, 2))
		.layer(l -> l.weight(1)
			.block(Blocks.DIORITE)
			.block(Blocks.CALCITE))
		.build();

	public static NonNullSupplier<LayerPattern> OCHRESTONE = () -> LayerPattern.builder()
		.layer(l -> l.weight(1)
			.block(Blocks.ANDESITE)
			.passiveBlock())
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.OCHRUM.getBaseBlock())
			.size(1, 3))
		.layer(l -> l.weight(2)
			.block(Blocks.TUFF)
			.block(Blocks.SMOOTH_BASALT)
			.size(1, 2))
		.layer(l -> l.weight(2)
			.block(Blocks.DRIPSTONE_BLOCK)
			.block(Blocks.GRANITE)
			.size(1, 2))
		.build();

	public static NonNullSupplier<LayerPattern> MALACHITE = () -> LayerPattern.builder()
		.layer(l -> l.weight(1)
			.passiveBlock())
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.VERIDIUM.getBaseBlock())
			.size(1, 3))
		.layer(l -> l.weight(2)
			.block(Blocks.TUFF)
			.block(Blocks.ANDESITE)
			.size(1, 2))
		.layer(l -> l.weight(1)
			.block(Blocks.SMOOTH_BASALT))
		.build();

	public static NonNullSupplier<LayerPattern> LIMESTONE = () -> LayerPattern.builder()
		.layer(l -> l.weight(1)
			.passiveBlock())
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.LIMESTONE.getBaseBlock())
			.size(1, 4))
		.build();

	public static NonNullSupplier<LayerPattern> SCORIA = () -> LayerPattern.builder()
		.inNether()
		.layer(l -> l.weight(1)
			.passiveBlock())
		.layer(l -> l.weight(2)
			.block(AllPaletteStoneTypes.SCORIA.getBaseBlock())
			.size(1, 4))
		.layer(l -> l.weight(1)
			.block(Blocks.BLACKSTONE)
			.size(1, 3))
		.layer(l -> l.weight(1)
			.block(Blocks.SMOOTH_BASALT))
		.build();

}
