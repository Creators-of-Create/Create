package com.simibubi.create.lib.data;

import com.simibubi.create.AllItems;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Blocks;

public class DataGenerators {
	public static class BlockTagProvider extends BlockTagsProvider {
		public BlockTagProvider(DataGenerator dataGenerator) {
			super(dataGenerator);
		}

		@Override
		protected void addTags() {
			tag(Tags.Blocks.STONE).add(Blocks.STONE);
		}
	}

	public static class ItemTagProvider extends ItemTagsProvider {

		public ItemTagProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagsProvider) {
			super(dataGenerator, blockTagsProvider);
		}

		@Override
		protected void addTags() {
			tag(Tags.Items.COPPER_PLATES).add(AllItems.COPPER_SHEET.get());
			tag(Tags.Items.IRON_PLATES).add(AllItems.IRON_SHEET.get());
		}
	}

	public static class FluidTagProvider extends FluidTagsProvider {
		public FluidTagProvider(DataGenerator dataGenerator) {
			super(dataGenerator);
		}

		@Override
		protected void addTags() {
			tag(Tags.Fluids.MILK);
		}
	}

	public static void gatherData(DataGenerator generator) {
		BlockTagsProvider blockTags = new BlockTagProvider(generator);
		generator.addProvider(blockTags);
		generator.addProvider(new ItemTagProvider(generator, blockTags));
		generator.addProvider(new FluidTagProvider(generator));
	}
}
