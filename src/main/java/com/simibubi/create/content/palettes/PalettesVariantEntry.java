package com.simibubi.create.content.palettes;

import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.ColorHandlers;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.tags.Tag;

public class PalettesVariantEntry {

	public final ImmutableList<BlockEntry<? extends Block>> registeredBlocks;
	public final ImmutableList<BlockEntry<? extends Block>> registeredPartials;

	public PalettesVariantEntry(PaletteStoneVariants variant, PaletteBlockPattern[] patterns) {
		String name = Lang.asId(variant.name());
		NonNullSupplier<Block> initialProperties = variant.getBaseBlock();
		ImmutableList.Builder<BlockEntry<? extends Block>> registeredBlocks = ImmutableList.builder();
		ImmutableList.Builder<BlockEntry<? extends Block>> registeredPartials = ImmutableList.builder();
		CreateRegistrate registrate = Create.registrate();

		for (PaletteBlockPattern pattern : patterns) {
			BlockBuilder<? extends Block, CreateRegistrate> builder =
				registrate.block(pattern.createName(name), pattern.getBlockFactory())
					.initialProperties(initialProperties)
					.blockstate(pattern.getBlockStateGenerator()
						.apply(pattern)
						.apply(name)::accept);

			ItemBuilder<BlockItem, ? extends BlockBuilder<? extends Block, CreateRegistrate>> itemBuilder =
				builder.item();

			Tag.Named<Block>[] blockTags = pattern.getBlockTags();
			if (blockTags != null) {
				builder.tag(blockTags);
			}
			Tag.Named<Item>[] itemTags = pattern.getItemTags();
			if (itemTags != null) {
				itemBuilder.tag(itemTags);
			}

			if (pattern.isTranslucent())
				builder.addLayer(() -> RenderType::translucent);
			if (pattern.hasFoliage()) {
				builder.color(() -> ColorHandlers::getGrassyBlock);
				itemBuilder.color(() -> ColorHandlers::getGrassyItem);
			}
			pattern.createCTBehaviour(variant)
				.ifPresent(b -> builder.onRegister(connectedTextures(b)));

			builder.recipe((c, p) -> {
				p.stonecutting(DataIngredient.items(variant.getBaseBlock()
					.get()), c::get);
				pattern.addRecipes(variant, c, p);
			});

			itemBuilder.register();
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
