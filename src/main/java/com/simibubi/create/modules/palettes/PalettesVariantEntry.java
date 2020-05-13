package com.simibubi.create.modules.palettes;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;

public class PalettesVariantEntry {

	public ImmutableList<BlockEntry<? extends Block>> registeredBlocks;

	public PalettesVariantEntry(String name, PaletteBlockPatterns[] patterns,
			NonNullFunction<BlockBuilder<? extends Block, PalettesRegistrate>, BlockEntry<? extends Block>> registerFunc) {

		ImmutableList.Builder<BlockEntry<? extends Block>> registeredBlocks = ImmutableList.builder();
		for (PaletteBlockPatterns pattern : patterns) {

			BlockBuilder<? extends Block, PalettesRegistrate> builder = Create.palettesRegistrate()
					.block(pattern.createName(name), pattern.getBlockFactory())
					.blockstate(pattern.getBlockStateGenerator()
							.apply(pattern)
							.apply(name)::accept);

			if (pattern.isTranslucent())
				builder.addLayer(() -> RenderType::getTranslucent);

			registeredBlocks.add(registerFunc.apply(builder));
		}
		this.registeredBlocks = registeredBlocks.build();

	}

}
