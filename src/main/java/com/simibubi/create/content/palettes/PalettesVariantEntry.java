package com.simibubi.create.content.palettes;

import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.ColorHandlers;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;

public class PalettesVariantEntry {

	public ImmutableList<BlockEntry<? extends Block>> registeredBlocks;
	public ImmutableList<BlockEntry<? extends Block>> registeredPartials;

	public PalettesVariantEntry(PaletteStoneVariants variant, PaletteBlockPattern[] patterns,
		NonNullSupplier<? extends Block> initialProperties) {
		String name = Lang.asId(variant.name());
		ImmutableList.Builder<BlockEntry<? extends Block>> registeredBlocks = ImmutableList.builder();
		ImmutableList.Builder<BlockEntry<? extends Block>> registeredPartials = ImmutableList.builder();

		for (PaletteBlockPattern pattern : patterns) {
			CreateRegistrate registrate = Create.registrate();
			BlockBuilder<? extends Block, CreateRegistrate> builder =
				registrate.block(pattern.createName(name), pattern.getBlockFactory())
					.initialProperties(initialProperties)
					.blockstate(pattern.getBlockStateGenerator()
						.apply(pattern)
						.apply(name)::accept);

			if (pattern.isTranslucent())
				builder.addLayer(() -> RenderType::getTranslucent);
			if (pattern == PaletteBlockPattern.COBBLESTONE)
				builder.item().tag(AllTags.AllItemTags.COBBLESTONE.tag);
			if (pattern.hasFoliage())
				builder.color(() -> ColorHandlers::getGrassyBlock);
			pattern.createCTBehaviour(variant)
				.ifPresent(b -> builder.onRegister(connectedTextures(b)));

			builder.recipe((c, p) -> {
				p.stonecutting(DataIngredient.items(variant.getBaseBlock()
					.get()), c::get);
				pattern.addRecipes(variant, c, p);
			});

			if (pattern.hasFoliage())
				builder.item()
						.color(() -> ColorHandlers::getGrassyItem)
					.build();
			else
				builder.simpleItem();

			BlockEntry<? extends Block> block = builder.register();
			registeredBlocks.add(block);

			for (PaletteBlockPartial<? extends Block> partialBlock : pattern.getPartials())
				registeredPartials.add(partialBlock.create(name, pattern, block)
					.register());
		}

		this.registeredBlocks = registeredBlocks.build();
		this.registeredPartials = registeredPartials.build();
	}

}
