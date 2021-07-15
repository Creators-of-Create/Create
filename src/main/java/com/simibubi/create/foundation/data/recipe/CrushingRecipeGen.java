package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.GeneratedRecipe;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

public class CrushingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	BLAZE_ROD = create(() -> Items.BLAZE_ROD, b -> b.duration(100)
		.output(Items.BLAZE_POWDER, 3)
		.output(.25f, Items.BLAZE_POWDER, 3)),

		PRISMARINE_CRYSTALS = create(() -> Items.PRISMARINE_CRYSTALS, b -> b.duration(150)
			.output(1f, Items.QUARTZ, 1)
			.output(.5f, Items.QUARTZ, 2)
			.output(.1f, Items.GLOWSTONE_DUST, 2)),

		OBSIDIAN = create(() -> Blocks.OBSIDIAN, b -> b.duration(500)
			.output(AllItems.POWDERED_OBSIDIAN.get())
			.output(.75f, Blocks.OBSIDIAN)),

		WOOL = create("wool", b -> b.duration(100)
			.require(ItemTags.WOOL)
			.output(Items.STRING, 2)
			.output(.5f, Items.STRING)),

		COPPER_BLOCK = create("copper_block", b -> b.duration(400)
			.require(I.copperBlock())
			.output(AllItems.CRUSHED_COPPER.get(), 5)),

		ZINC_BLOCK = create("zinc_block", b -> b.duration(400)
			.require(I.zincBlock())
			.output(AllItems.CRUSHED_ZINC.get(), 5)),

		BRASS_BLOCK = create("brass_block", b -> b.duration(400)
			.require(I.brassBlock())
			.output(AllItems.CRUSHED_BRASS.get(), 5)),

		COPPER_ORE = metalOre("copper", AllItems.CRUSHED_COPPER, 350),
		ZINC_ORE = metalOre("zinc", AllItems.CRUSHED_ZINC, 350),
		IRON_ORE = metalOre("iron", AllItems.CRUSHED_IRON, 400),
		GOLD_ORE = metalOre("gold", AllItems.CRUSHED_GOLD, 300),

		OSMIUM_ORE = metalOre("osmium", AllItems.CRUSHED_OSMIUM, 400),
		PLATINUM_ORE = metalOre("platinum", AllItems.CRUSHED_PLATINUM, 300),
		SILVER_ORE = metalOre("silver", AllItems.CRUSHED_SILVER, 300),
		TIN_ORE = metalOre("tin", AllItems.CRUSHED_TIN, 350),
		QUICKSILVER_ORE = metalOre("quicksilver", AllItems.CRUSHED_QUICKSILVER, 300),
		LEAD_ORE = metalOre("lead", AllItems.CRUSHED_LEAD, 400),
		ALUMINUM_ORE = metalOre("aluminum", AllItems.CRUSHED_BAUXITE, 300),
		URANIUM_ORE = metalOre("uranium", AllItems.CRUSHED_URANIUM, 400),
		NICKEL_ORE = metalOre("nickel", AllItems.CRUSHED_NICKEL, 350),

		NETHER_QUARTZ_ORE = create(() -> Blocks.NETHER_QUARTZ_ORE, b -> b.duration(350)
			.output(Items.QUARTZ, 2)
			.output(.5f, Items.QUARTZ, 4)
			.output(.125f, Blocks.NETHERRACK)),

		REDSTONE_ORE = create(() -> Blocks.REDSTONE_ORE, b -> b.duration(300)
			.output(Items.REDSTONE, 8)
			.output(.25f, Items.REDSTONE, 6)
			.output(.125f, Blocks.COBBLESTONE)),

		LAPIS_ORE = create(() -> Blocks.LAPIS_ORE, b -> b.duration(300)
			.output(Items.LAPIS_LAZULI, 12)
			.output(.25f, Items.LAPIS_LAZULI, 8)
			.output(.125f, Blocks.COBBLESTONE)),

		COAL_ORE = create(() -> Blocks.COAL_ORE, b -> b.duration(300)
			.output(Items.COAL, 2)
			.output(.5f, Items.COAL, 2)
			.output(.125f, Blocks.COBBLESTONE)),

		EMERALD_ORE = create(() -> Blocks.EMERALD_ORE, b -> b.duration(500)
			.output(Items.EMERALD, 2)
			.output(.25f, Items.EMERALD, 1)
			.output(.125f, Blocks.COBBLESTONE)),

		DIAMOND_ORE = create(() -> Blocks.DIAMOND_ORE, b -> b.duration(500)
			.output(Items.DIAMOND, 2)
			.output(.25f, Items.DIAMOND, 1)
			.output(.125f, Blocks.COBBLESTONE)),

		NETHER_WART = create("nether_wart_block", b -> b.duration(150)
			.require(Blocks.NETHER_WART_BLOCK)
			.output(.25f, Items.NETHER_WART, 1)),

		GLOWSTONE = create(() -> Blocks.GLOWSTONE, b -> b.duration(150)
			.output(Items.GLOWSTONE_DUST, 3)
			.output(.5f, Items.GLOWSTONE_DUST)),

		LEATHER_HORSE_ARMOR = create(() -> Items.LEATHER_HORSE_ARMOR, b -> b.duration(200)
			.output(Items.LEATHER, 2)
			.output(.5f, Items.LEATHER, 2)),

		IRON_HORSE_ARMOR = create(() -> Items.IRON_HORSE_ARMOR, b -> b.duration(200)
			.output(Items.IRON_INGOT, 2)
			.output(.5f, Items.LEATHER, 1)
			.output(.5f, Items.IRON_INGOT, 1)
			.output(.25f, Items.STRING, 2)
			.output(.25f, Items.IRON_NUGGET, 4)),

		GOLDEN_HORSE_ARMOR = create(() -> Items.GOLDEN_HORSE_ARMOR, b -> b.duration(200)
			.output(Items.GOLD_INGOT, 2)
			.output(.5f, Items.LEATHER, 2)
			.output(.5f, Items.GOLD_INGOT, 2)
			.output(.25f, Items.STRING, 2)
			.output(.25f, Items.GOLD_NUGGET, 8)),

		DIAMOND_HORSE_ARMOR = create(() -> Items.DIAMOND_HORSE_ARMOR, b -> b.duration(200)
			.output(Items.DIAMOND, 1)
			.output(.5f, Items.LEATHER, 2)
			.output(.1f, Items.DIAMOND, 3)
			.output(.25f, Items.STRING, 2)),

		GRAVEL = create(() -> Blocks.GRAVEL, b -> b.duration(250)
			.output(Blocks.SAND)
			.output(.1f, Items.FLINT)
			.output(.05f, Items.CLAY_BALL)),

		SAND = create(() -> Blocks.SAND, b -> b.duration(150)
			.output(AllPaletteBlocks.LIMESAND.get())
			.output(.1f, Items.BONE_MEAL)),

		NETHERRACK = create(() -> Blocks.NETHERRACK, b -> b.duration(250)
			.output(AllItems.CINDER_FLOUR.get())
			.output(.5f, AllItems.CINDER_FLOUR.get()))

	;

	protected GeneratedRecipe metalOre(String name, ItemEntry<? extends Item> crushed, int duration) {
		return create(name + "_ore", b -> b.duration(duration)
			.withCondition(new NotCondition(new TagEmptyCondition("forge", "ores/" + name)))
			.require(AllTags.forgeItemTag("ores/" + name))
			.output(crushed.get())
			.output(.3f, crushed.get(), 2)
			.output(.125f, Blocks.COBBLESTONE));
	}

	public CrushingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CRUSHING;
	}

}
