package com.simibubi.create.lib.data;

import com.simibubi.create.AllInteractionBehaviours;
import com.simibubi.create.lib.utility.TagUtil;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

//TODO: IMPLEMENT ALL THESE TAGS WITH COMMON ONES
public class Tags {
	public static class Items {
		public static Tag.Named<Item> tag(String id) {
			return TagFactory.ITEM.create(new ResourceLocation("c", id));
		}
		public static final Tag.Named<Item> STONE = tag("stone");
		public static final Tag.Named<Item> COBBLESTONE = tag("");
		public static final Tag.Named<Item> ORES = tag("ores");
		public static final Tag.Named<Item> NUGGETS = tag("nuggets");
		public static final Tag.Named<Item> NUGGETS_IRON = tag("iron_nuggets");
		public static final Tag.Named<Item> DYES = tag("");
		public static final Tag.Named<Item> SLIMEBALLS = tag("slimeballs");
		public static final Tag.Named<Item> GLASS_COLORLESS = tag("");
		public static final Tag.Named<Item> DUSTS_REDSTONE = tag("redstone_dusts");
		public static final Tag.Named<Item> INGOTS_IRON = tag("iron_ingots");
		public static final Tag.Named<Item> IRON_PLATES = tag("iron_plates");
		public static final Tag.Named<Item> INGOTS_BRASS = tag("brass_ingots");
		public static final Tag.Named<Item> RODS_WOODEN = tag("wooden_rods");
		public static final Tag.Named<Item> ZINC_ORES = tag("zinc_ores");
		public static final Tag.Named<Item> DUSTS_GLOWSTONE = tag("glowstone_dust");
		public static final Tag.Named<Item> GLASS_PANES = tag("");
		public static final Tag.Named<Item> STAINED_GLASS = tag("");
		public static final Tag.Named<Item> STAINED_GLASS_PANES = tag("");
		public static final Tag.Named<Item> COPPER_PLATES = tag("copper_plates");
		public static final Tag.Named<Item> OBSIDIAN = tag("");
		public static final Tag.Named<Item> STORAGE_BLOCKS = tag("");
		public static final Tag.Named<Item> ENDER_PEARLS = tag("");
		public static final Tag.Named<Item> GLASS = tag("");
	}
	public static class Blocks {
		public static Tag.Named<Block> tag(String id) {
			return TagFactory.BLOCK.create(new ResourceLocation("c", id));
		}
		public static final Tag.Named<Block> STONE = tag("");
		public static final Tag.Named<Block> COBBLESTONE = tag("");
		public static final Tag.Named<Block> ORES = tag("ores");
		public static final Tag.Named<Block> GLASS_COLORLESS = tag("");
		public static final Tag.Named<Block> GLASS_PANES = tag("");
		public static final Tag.Named<Block> STORAGE_BLOCKS = tag("");
	}
	public static class Fluids {
		public static Tag.Named<Fluid> tag(String id) {
			return TagFactory.FLUID.create(new ResourceLocation("c", id));
		}
		public static final Tag.Named<Fluid> MILK = tag("milk");


	}
}
