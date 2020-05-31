package com.simibubi.create.content.palettes;

import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllColorHandlers;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
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

	public PalettesVariantEntry(PaletteStoneVariants variant, PaletteBlockPatterns[] patterns,
		NonNullSupplier<? extends Block> initialProperties) {

		String name = Lang.asId(variant.name());
		ImmutableList.Builder<BlockEntry<? extends Block>> registeredBlocks = ImmutableList.builder();
		ImmutableList.Builder<BlockEntry<? extends Block>> registeredPartials = ImmutableList.builder();
		for (PaletteBlockPatterns pattern : patterns) {

			CreateRegistrate registrate = Create.registrate();
			BlockBuilder<? extends Block, CreateRegistrate> builder =
				registrate.block(pattern.createName(name), pattern.getBlockFactory())
					.initialProperties(initialProperties)
					.blockstate(pattern.getBlockStateGenerator()
						.apply(pattern)
						.apply(name)::accept);

			if (pattern.isTranslucent())
				builder.addLayer(() -> RenderType::getTranslucent);
			if (pattern.hasFoliage())
				builder.onRegister(CreateRegistrate.blockColors(() -> AllColorHandlers::getGrassyBlock));
			pattern.createCTBehaviour(variant)
				.ifPresent(b -> builder.onRegister(connectedTextures(b)));
			
			builder.recipe((c, p) -> {
				p.stonecutting(DataIngredient.items(variant.getBaseBlock().get()), c::get);
				pattern.addRecipes(variant, c, p);
			});

			if (pattern.hasFoliage())
				builder.item()
					.onRegister(CreateRegistrate.itemColors(() -> AllColorHandlers::getGrassyItem))
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
