package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.CompatMetals.ALUMINUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.LEAD;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.NICKEL;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.OSMIUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.PLATINUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.QUICKSILVER;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.SILVER;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.TIN;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.URANIUM;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.registries.ForgeRegistries;

public class CrushingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	BLAZE_ROD = create(() -> Items.BLAZE_ROD, b -> b.duration(100)
		.output(Items.BLAZE_POWDER, 3)
		.output(.25f, Items.BLAZE_POWDER, 3)),

		PRISMARINE_CRYSTALS = create(() -> Items.PRISMARINE_CRYSTALS, b -> b.duration(150)
			.output(1f, Items.QUARTZ, 1)
			.output(.5f, Items.QUARTZ, 2)
			.output(.1f, Items.GLOWSTONE_DUST, 2)),

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

		WOOL = create("wool", b -> b.duration(100)
			.require(ItemTags.WOOL)
			.output(Items.STRING, 2)
			.output(.5f, Items.STRING)),

		NETHER_WART = create("nether_wart_block", b -> b.duration(150)
			.require(Blocks.NETHER_WART_BLOCK)
			.output(.25f, Items.NETHER_WART, 1)),

		AMETHYST_CLUSTER = create(() -> Blocks.AMETHYST_CLUSTER, b -> b.duration(150)
			.output(Items.AMETHYST_SHARD, 7)
			.output(.5f, Items.AMETHYST_SHARD)),

		GLOWSTONE = create(() -> Blocks.GLOWSTONE, b -> b.duration(150)
			.output(Items.GLOWSTONE_DUST, 3)
			.output(.5f, Items.GLOWSTONE_DUST)),

		AMETHYST_BLOCK = create(() -> Blocks.AMETHYST_BLOCK, b -> b.duration(150)
			.output(Items.AMETHYST_SHARD, 3)
			.output(.5f, Items.AMETHYST_SHARD)),

		GRAVEL = create(() -> Blocks.GRAVEL, b -> b.duration(250)
			.output(Blocks.SAND)
			.output(.1f, Items.FLINT)
			.output(.05f, Items.CLAY_BALL)),

		NETHERRACK = create(() -> Blocks.NETHERRACK, b -> b.duration(250)
			.output(AllItems.CINDER_FLOUR.get())
			.output(.5f, AllItems.CINDER_FLOUR.get())
			.whenModMissing(Mods.ENS.getId())),

		OBSIDIAN = create(() -> Blocks.OBSIDIAN, b -> b.duration(500)
			.output(AllItems.POWDERED_OBSIDIAN.get())
			.output(.75f, Blocks.OBSIDIAN)),

		DIORITE = ensMineralRecycling(AllPaletteStoneTypes.DIORITE, b -> b.duration(350)
			.output(.25f, Items.QUARTZ, 1)),

		CRIMSITE =
			mineralRecycling(AllPaletteStoneTypes.CRIMSITE, AllItems.CRUSHED_IRON::get, () -> Items.IRON_NUGGET, .4f),

		VERIDIUM = mineralRecycling(AllPaletteStoneTypes.VERIDIUM, AllItems.CRUSHED_COPPER::get,
			() -> AllItems.COPPER_NUGGET::get, .8f),

		ASURINE = mineralRecycling(AllPaletteStoneTypes.ASURINE, AllItems.CRUSHED_ZINC::get,
			() -> AllItems.ZINC_NUGGET::get, .3f),

		OCHRUM =
			mineralRecycling(AllPaletteStoneTypes.OCHRUM, AllItems.CRUSHED_GOLD::get, () -> Items.GOLD_NUGGET, .2f),

		TUFF = mineralRecycling(AllPaletteStoneTypes.TUFF, b -> b.duration(350)
			.output(.25f, Items.FLINT, 1)
			.output(.1f, Items.GOLD_NUGGET, 1)
			.output(.1f, AllItems.COPPER_NUGGET.get(), 1)
			.output(.1f, AllItems.ZINC_NUGGET.get(), 1)
			.output(.1f, Items.IRON_NUGGET, 1)),

		COAL_ORE = stoneOre(() -> Items.COAL_ORE, () -> Items.COAL, 1.75f, 150),
		IRON_ORE = stoneOre(() -> Items.IRON_ORE, AllItems.CRUSHED_IRON::get, 1.75f, 250),
		COPPER_ORE = stoneOre(() -> Items.COPPER_ORE, AllItems.CRUSHED_COPPER::get, 5.25f, 250),
		GOLD_ORE = stoneOre(() -> Items.GOLD_ORE, AllItems.CRUSHED_GOLD::get, 1.75f, 250),
		REDSTONE_ORE = stoneOre(() -> Items.REDSTONE_ORE, () -> Items.REDSTONE, 6.5f, 250),
		EMERALD_ORE = stoneOre(() -> Items.EMERALD_ORE, () -> Items.EMERALD, 1.75f, 350),
		LAPIS_ORE = stoneOre(() -> Items.LAPIS_ORE, () -> Items.LAPIS_LAZULI, 10.5f, 250),
		DIAMOND_ORE = stoneOre(() -> Items.DIAMOND_ORE, () -> Items.DIAMOND, 1.75f, 350),
		ZINC_ORE = stoneOre(AllBlocks.ZINC_ORE::get, AllItems.CRUSHED_ZINC::get, 1.75f, 250),

		DEEP_COAL_ORE = deepslateOre(() -> Items.DEEPSLATE_COAL_ORE, () -> Items.COAL, 2.25f, 300),
		DEEP_IRON_ORE = deepslateOre(() -> Items.DEEPSLATE_IRON_ORE, AllItems.CRUSHED_IRON::get, 2.25f, 350),
		DEEP_COPPER_ORE = deepslateOre(() -> Items.DEEPSLATE_COPPER_ORE, AllItems.CRUSHED_COPPER::get, 7.25f, 350),
		DEEP_GOLD_ORE = deepslateOre(() -> Items.DEEPSLATE_GOLD_ORE, AllItems.CRUSHED_GOLD::get, 2.25f, 350),
		DEEP_REDSTONE_ORE = deepslateOre(() -> Items.DEEPSLATE_REDSTONE_ORE, () -> Items.REDSTONE, 7.5f, 350),
		DEEP_EMERALD_ORE = deepslateOre(() -> Items.DEEPSLATE_EMERALD_ORE, () -> Items.EMERALD, 2.25f, 450),
		DEEP_LAPIS_ORE = deepslateOre(() -> Items.DEEPSLATE_LAPIS_ORE, () -> Items.LAPIS_LAZULI, 12.5f, 350),
		DEEP_DIAMOND_ORE = deepslateOre(() -> Items.DEEPSLATE_DIAMOND_ORE, () -> Items.DIAMOND, 2.25f, 450),
		DEEP_ZINC_ORE = deepslateOre(AllBlocks.DEEPSLATE_ZINC_ORE::get, AllItems.CRUSHED_ZINC::get, 2.25f, 350),

		NETHER_GOLD_ORE = netherOre(() -> Items.NETHER_GOLD_ORE, () -> Items.GOLD_NUGGET, 18, 350),
		NETHER_QUARTZ_ORE = netherOre(() -> Items.NETHER_QUARTZ_ORE, () -> Items.QUARTZ, 2.25f, 350),

		GILDED_BLACKSTONE = ore(Items.BLACKSTONE, () -> Items.GILDED_BLACKSTONE, () -> Items.GOLD_NUGGET, 18, 400),

		OSMIUM_ORE = moddedOre(OSMIUM, AllItems.CRUSHED_OSMIUM::get),
		PLATINUM_ORE = moddedOre(PLATINUM, AllItems.CRUSHED_PLATINUM::get),
		SILVER_ORE = moddedOre(SILVER, AllItems.CRUSHED_SILVER::get),
		TIN_ORE = moddedOre(TIN, AllItems.CRUSHED_TIN::get),
		QUICKSILVER_ORE = moddedOre(QUICKSILVER, AllItems.CRUSHED_QUICKSILVER::get),
		LEAD_ORE = moddedOre(LEAD, AllItems.CRUSHED_LEAD::get),
		ALUMINUM_ORE = moddedOre(ALUMINUM, AllItems.CRUSHED_BAUXITE::get),
		URANIUM_ORE = moddedOre(URANIUM, AllItems.CRUSHED_URANIUM::get),
		NICKEL_ORE = moddedOre(NICKEL, AllItems.CRUSHED_NICKEL::get),

		RAW_IRON_ORE = rawOre("iron", () -> Tags.Items.RAW_MATERIALS_IRON, AllItems.CRUSHED_IRON::get, 1),
		RAW_COPPER_ORE = rawOre("copper", () -> Tags.Items.RAW_MATERIALS_COPPER, AllItems.CRUSHED_COPPER::get, 1),
		RAW_GOLD_ORE = rawOre("gold", () -> Tags.Items.RAW_MATERIALS_GOLD, AllItems.CRUSHED_GOLD::get, 2),
		RAW_ZINC_ORE = rawOre("zinc", () -> AllTags.forgeItemTag("raw_materials/zinc"), AllItems.CRUSHED_ZINC::get, 1),

		OSMIUM_RAW_ORE = moddedRawOre(OSMIUM, AllItems.CRUSHED_OSMIUM::get),
		PLATINUM_RAW_ORE = moddedRawOre(PLATINUM, AllItems.CRUSHED_PLATINUM::get),
		SILVER_RAW_ORE = moddedRawOre(SILVER, AllItems.CRUSHED_SILVER::get),
		TIN_RAW_ORE = moddedRawOre(TIN, AllItems.CRUSHED_TIN::get),
		QUICKSILVER_RAW_ORE = moddedRawOre(QUICKSILVER, AllItems.CRUSHED_QUICKSILVER::get),
		LEAD_RAW_ORE = moddedRawOre(LEAD, AllItems.CRUSHED_LEAD::get),
		ALUMINUM_RAW_ORE = moddedRawOre(ALUMINUM, AllItems.CRUSHED_BAUXITE::get),
		URANIUM_RAW_ORE = moddedRawOre(URANIUM, AllItems.CRUSHED_URANIUM::get),
		NICKEL_RAW_ORE = moddedRawOre(NICKEL, AllItems.CRUSHED_NICKEL::get),

		RAW_IRON_BLOCK = rawOreBlock("iron", () -> Tags.Items.STORAGE_BLOCKS_RAW_IRON, AllItems.CRUSHED_IRON::get, 1),
		RAW_COPPER_BLOCK = rawOreBlock("copper", () -> Tags.Items.STORAGE_BLOCKS_RAW_COPPER, AllItems.CRUSHED_COPPER::get, 1),
		RAW_GOLD_BLOCK = rawOreBlock("gold", () -> Tags.Items.STORAGE_BLOCKS_RAW_GOLD, AllItems.CRUSHED_GOLD::get, 2),
		RAW_ZINC_BLOCK = rawOreBlock("zinc", () -> AllTags.forgeItemTag("storage_blocks/raw_zinc"), AllItems.CRUSHED_ZINC::get, 1),

		OSMIUM_RAW_BLOCK = moddedRawOreBlock(OSMIUM, AllItems.CRUSHED_OSMIUM::get),
		PLATINUM_RAW_BLOCK = moddedRawOreBlock(PLATINUM, AllItems.CRUSHED_PLATINUM::get),
		SILVER_RAW_BLOCK = moddedRawOreBlock(SILVER, AllItems.CRUSHED_SILVER::get),
		TIN_RAW_BLOCK = moddedRawOreBlock(TIN, AllItems.CRUSHED_TIN::get),
		QUICKSILVER_RAW_BLOCK = moddedRawOreBlock(QUICKSILVER, AllItems.CRUSHED_QUICKSILVER::get),
		LEAD_RAW_BLOCK = moddedRawOreBlock(LEAD, AllItems.CRUSHED_LEAD::get),
		ALUMINUM_RAW_BLOCK = moddedRawOreBlock(ALUMINUM, AllItems.CRUSHED_BAUXITE::get),
		URANIUM_RAW_BLOCK = moddedRawOreBlock(URANIUM, AllItems.CRUSHED_URANIUM::get),
		NICKEL_RAW_BLOCK = moddedRawOreBlock(NICKEL, AllItems.CRUSHED_NICKEL::get),

		// Oh The Biomes You'll Go
		BYG_AMETRINE_ORE = create(Mods.BYG.recipeId("ametrine_ore"), b -> b.duration(500)
				.require(AllTags.optionalTag(ForgeRegistries.ITEMS,
						new ResourceLocation("forge", "ores/ametrine")))
				.output(1f, Mods.BYG, "ametrine_gems", 2)
				.output(.25f, Mods.BYG, "ametrine_gems", 1)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "cobbled_ether_stone", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_ANTHRACITE_ORE = create(Mods.BYG.recipeId("anthracite_ore"), b -> b.duration(150)
				.require(AllTags.optionalTag(ForgeRegistries.ITEMS,
						new ResourceLocation("forge", "ores/anthracite")))
				.output(1f, Mods.BYG, "anthracite", 2)
				.output(.5f, Mods.BYG, "anthracite", 1)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "brimstone", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_BLUE_GOLD_ORE = create(Mods.BYG.recipeId("blue_nether_gold_ore"), b -> b.duration(350)
				.require(Mods.BYG, "blue_nether_gold_ore")
				.output(1f, Items.GOLD_NUGGET, 18)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "blue_netherrack", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_BLUE_QUARTZ_ORE = create(Mods.BYG.recipeId("blue_nether_quartz_ore"), b -> b.duration(350)
				.require(Mods.BYG, "blue_nether_quartz_ore")
				.output(1f, Items.QUARTZ, 2)
				.output(.25f, Items.QUARTZ, 1)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "blue_netherrack", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_BRIMSTONE_GOLD_ORE = create(Mods.BYG.recipeId("brimstone_nether_gold_ore"), b -> b.duration(350)
				.require(Mods.BYG, "brimstone_nether_gold_ore")
				.output(1f, Items.GOLD_NUGGET, 18)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "brimstone", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_BRIMSTONE_QUARTZ_ORE = create(Mods.BYG.recipeId("brimstone_nether_quartz_ore"), b -> b.duration(350)
				.require(Mods.BYG, "brimstone_nether_quartz_ore")
				.output(1f, Items.QUARTZ, 2)
				.output(.25f, Items.QUARTZ, 1)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "brimstone", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_REDSTONE_ORE = create(Mods.BYG.recipeId("cryptic_redstone_ore"), b -> b.duration(250)
				.require(Mods.BYG, "cryptic_redstone_ore")
				.output(1f, Items.REDSTONE, 6)
				.output(.5f, Items.REDSTONE, 1)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "cryptic_stone", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_EMERALDITE_ORE = create(Mods.BYG.recipeId("emeraldite_ore"), b -> b.duration(500)
				.require(AllTags.forgeItemTag("ores/emeraldite"))
				.output(1f,Mods.BYG, "emeraldite_shards", 2)
				.output(.25f, Mods.BYG, "emeraldite_shards", 1)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "scoria_cobblestone", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_LIGNITE_ORE = create(Mods.BYG.recipeId("lignite_ore"), b -> b.duration(300)
				.require(AllTags.forgeItemTag("ores/lignite"))
				.output(1f,Mods.BYG, "lignite", 2)
				.output(.5f, Mods.BYG, "lignite", 2)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Mods.BYG, "cobbled_ether_stone", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_NETHERRACK_ORE = create(Mods.BYG.recipeId("pervaded_netherrack"), b -> b.duration(150)
				.require(AllTags.forgeItemTag("ores/emeraldite"))
				.output(1f, Items.GLOWSTONE, 2)
				.output(.5f, Items.GLOWSTONE, 1)
				.output(.75f, AllItems.EXP_NUGGET.get(), 1)
				.output(.125f, Items.NETHERRACK, 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_RED_ROCK_ORE = create(Mods.BYG.recipeId("red_rock"), b -> b.duration(150)
				.require(Mods.BYG, "red_rock")
				.output(1f, Items.RED_SAND, 1)
				.whenModLoaded(Mods.BYG.getId())),

		// Druidcraft

		DC_AMBER_ORE = create(Mods.DRUIDCRAFT.recipeId("amber_ore"), b -> b.duration(300)
				.require(Mods.DRUIDCRAFT, "amber_ore")
				.output(1f, Mods.DRUIDCRAFT, "amber", 2)
				.output(.5f, Mods.DRUIDCRAFT, "amber", 1)
				.output(.125f, Items.COBBLESTONE, 1)
				.whenModLoaded(Mods.DRUIDCRAFT.getId())),

		DC_FIERY_GLASS_ORE = create(Mods.DRUIDCRAFT.recipeId("fiery_glass_ore"), b -> b.duration(300)
				.require(Mods.DRUIDCRAFT, "fiery_glass_ore")
				.output(1f, Mods.DRUIDCRAFT, "fiery_glass", 8)
				.output(.25f, Mods.DRUIDCRAFT, "fiery_glass", 6)
				.output(.125f, Items.COBBLESTONE, 1)
				.whenModLoaded(Mods.DRUIDCRAFT.getId())),

		DC_MOONSTONE_ORE = create(Mods.DRUIDCRAFT.recipeId("moonstone_ore"), b -> b.duration(300)
				.require(Mods.DRUIDCRAFT, "moonstone_ore")
				.output(1f, Mods.DRUIDCRAFT, "moonstone", 2)
				.output(.5f, Mods.DRUIDCRAFT, "moonstone", 1)
				.output(.125f, Items.COBBLESTONE, 1)
				.whenModLoaded(Mods.DRUIDCRAFT.getId())),

		// Neapolitan

		NEA_ICE = create(Mods.NEA.recipeId("ice"), b -> b.duration(100)
				.require(Items.ICE)
				.output(1f, Mods.NEA, "ice_cubes", 3)
				.output(.25f, Mods.NEA, "ice_cubes", 3)
				.whenModLoaded(Mods.NEA.getId())),

		// Quark

		Q_MOSS = create(Mods.Q.recipeId("moss_block"), b -> b.duration(50)
				.require(Items.MOSS_BLOCK)
				.output(1f, Mods.Q, "moss_paste", 2)
				.output(.1f, Mods.Q, "moss_paste", 1)
				.whenModLoaded(Mods.Q.getId())),

		// Silent Gems

		SG_STONE = sgStoneOres("peridot", "ruby", "sapphire", "topaz"),

		SG_NETHER = sgNetherOres("alexandrite", "black_diamond", "carnelian", "citrine", "iolite", "moldavite", "turquoise"),

		SG_END = sgEndOres("ammolite", "kyanite", "rose_quartz"),

		// Simple Farming

		SF = sfPlants("barley", "oat", "rice", "rye"),

		// Thermal Expansion

		TH = thOres("apatite", "cinnabar", "niter", "sulfur"),

		//Galosphere

		GS_ALLURITE = create(Mods.GS.recipeId("allurite"), b -> b.duration(300)
				.require(AllTags.AllItemTags.ALLURITE.tag)
				.output(.8f, Mods.GS, "allurite_shard", 4)
				.whenModLoaded(Mods.GS.getId())),

		GS_LUMIERE = create(Mods.GS.recipeId("lumiere"), b -> b.duration(300)
				.require(AllTags.AllItemTags.LUMIERE.tag)
				.output(.8f, Mods.GS, "lumiere_shard", 4)
				.whenModLoaded(Mods.GS.getId())),

		GS_AMETHYST = create(Mods.GS.recipeId("amethyst"), b -> b.duration(300)
				.require(AllTags.AllItemTags.AMETHYST.tag)
				.output(.8f, Items.AMETHYST_SHARD, 4)
				.whenModLoaded(Mods.GS.getId())),

		//Elementary Ores
		EO_COAL_NETHER = eoNetherOre("coal", Items.COAL, 1),
		EO_COPPER_NETHER = eoNetherOre("copper", AllItems.CRUSHED_COPPER.get(), 5),
		EO_IRON_NETHER = eoNetherOre("iron", AllItems.CRUSHED_IRON.get(), 1),
		EO_EMERALD_NETHER = eoNetherOre("emerald", Items.EMERALD, 1),
		EO_LAPIS_NETHER = eoNetherOre("lapis", Items.LAPIS_LAZULI, 10),
		EO_DIAMOND_NETHER = eoNetherOre("diamond", Items.DIAMOND, 1),
		EO_GHAST_NETHER = eoNetherOre("ghast", Items.GHAST_TEAR, 1),
		EO_COAL_END = eoEndOre("coal", Items.COAL, 1),
		EO_COPPER_END = eoEndOre("copper", AllItems.CRUSHED_COPPER.get(), 5),
		EO_EMERALD_END = eoEndOre("emerald", Items.EMERALD, 1),
		EO_LAPIS_END = eoEndOre("lapis", Items.LAPIS_LAZULI, 10),
		EO_DIAMOND_END = eoEndOre("diamond", Items.DIAMOND, 1),
		EO_REDSTONE_END = eoEndOre("redstone", Items.REDSTONE, 6),
		EO_ENDER_END = eoEndOre("ender", Items.ENDER_PEARL, 1),

		// Ex Nihilo: Sequentia

		ENS_STONES = ensStones("andesite", "diorite", "end_stone", "granite", "netherrack"),

		ENS_DUST = create(Mods.ENS.recipeId("dust"), b -> b.duration(200)
				.require(Blocks.SAND).output(Mods.ENS, "dust")
				.whenModLoaded(Mods.ENS.getId())),

		ENS_NETHERRACK = create(Mods.ENS.recipeId("crushed_netherrack"), b -> b.duration(100)
				.require(Mods.ENS, "crushed_netherrack")
				.output(AllItems.CINDER_FLOUR.get())
				.output(.5f, AllItems.CINDER_FLOUR.get())
				.whenModLoaded(Mods.ENS.getId())),

		ENS_DIORITE = create(Mods.ENS.recipeId("crushed_diorite"), b -> b.duration(100)
				.require(Mods.ENS, "crushed_diorite")
				.output(.25f, Items.QUARTZ, 1)
				.whenModLoaded(Mods.ENS.getId())),

		// Aether

		AET_ZANITE = create(Mods.AET.recipeId("zanite_ore"), b -> b.duration(350)
				.require(Mods.AET, "zanite_ore")
				.output(Mods.AET, "zanite_gemstone")
				.output(0.75f, Mods.AET, "zanite_gemstone", 1)
				.output(0.125f, Mods.AET, "holystone", 1)
				.output(0.75f, AllItems.EXP_NUGGET.get())
				.whenModLoaded(Mods.AET.getId())),

		AET_AMBROSIUM = create(Mods.AET.recipeId("ambrosium_ore"), b -> b.duration(150)
				.require(Mods.AET, "ambrosium_ore")
				.output(Mods.AET, "ambrosium_shard")
				.output(0.75f, Mods.AET, "ambrosium_shard", 1)
				.output(0.125f, Mods.AET, "holystone", 1)
				.output(0.75f, AllItems.EXP_NUGGET.get())
				.whenModLoaded(Mods.AET.getId())),

		// Deep Aether

		D_AET_SKYJADE = create(Mods.D_AET.recipeId("skyjade_ore"), b -> b.duration(150)
				.require(Mods.D_AET, "skyjade_ore")
				.output(Mods.D_AET, "skyjade")
				.output(0.75f, Mods.D_AET, "skyjade", 1)
				.output(0.125f, Mods.AET, "holystone", 1)
				.output(0.75f, AllItems.EXP_NUGGET.get())
				.whenModLoaded(Mods.D_AET.getId())),

		// Ancient Aether

		A_AET_QUARTZ = create(Mods.A_AET.recipeId("quartz_ore"), b -> b.duration(150)
				.require(Mods.A_AET, "aether_quartz_ore")
				.output(Items.QUARTZ)
				.output(0.75f, Items.QUARTZ, 1)
				.output(0.125f, Mods.AET, "holystone", 1)
				.output(0.75f, AllItems.EXP_NUGGET.get())
				.whenModLoaded(Mods.A_AET.getId()))

			;

	protected GeneratedRecipe mineralRecycling(AllPaletteStoneTypes type, Supplier<ItemLike> crushed,
		Supplier<ItemLike> nugget, float chance) {
		return mineralRecycling(type, b -> b.duration(250)
			.output(chance, crushed.get(), 1)
			.output(chance, nugget.get(), 1));
	}

	protected GeneratedRecipe mineralRecycling(AllPaletteStoneTypes type,
		UnaryOperator<ProcessingRecipeBuilder<ProcessingRecipe<?>>> transform) {
		create(Lang.asId(type.name()) + "_recycling", b -> transform.apply(b.require(type.materialTag)));
		return create(type.getBaseBlock()::get, transform);
	}

	protected GeneratedRecipe ensMineralRecycling(AllPaletteStoneTypes type,
											   UnaryOperator<ProcessingRecipeBuilder<ProcessingRecipe<?>>> transform) {
		create(Lang.asId(type.name()) + "_recycling", b -> transform.apply(b.require(type.materialTag)));
		return create(type.getBaseBlock()::get, b -> transform.apply(b.whenModMissing(Mods.ENS.getId())));
	}

	protected GeneratedRecipe stoneOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
		int duration) {
		return ore(Blocks.COBBLESTONE, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe deepslateOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
		int duration) {
		return ore(Blocks.COBBLED_DEEPSLATE, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe netherOre(Supplier<ItemLike> ore, Supplier<ItemLike> raw, float expectedAmount,
		int duration) {
		return ore(Blocks.NETHERRACK, ore, raw, expectedAmount, duration);
	}

	protected GeneratedRecipe ore(ItemLike stoneType, Supplier<ItemLike> ore, Supplier<ItemLike> raw,
		float expectedAmount, int duration) {
		return create(ore, b -> {
			ProcessingRecipeBuilder<ProcessingRecipe<?>> builder = b.duration(duration)
				.output(raw.get(), Mth.floor(expectedAmount));
			float extra = expectedAmount - Mth.floor(expectedAmount);
			if (extra > 0)
				builder.output(extra, raw.get(), 1);
			builder.output(.75f, AllItems.EXP_NUGGET.get(), raw.get() == AllItems.CRUSHED_GOLD.get() ? 2 : 1);
			return builder.output(.125f, stoneType);
		});
	}

	protected GeneratedRecipe moddedOre(CompatMetals metal, Supplier<ItemLike> result) {
		String name = metal.getName();
		return create(name + "_ore", b -> {
			String prefix = "ores/";
			return b.duration(400)
				.withCondition(new NotCondition(new TagEmptyCondition("forge", prefix + name)))
				.require(AllTags.forgeItemTag(prefix + name))
				.output(result.get(), 1)
				.output(.75f, result.get(), 1)
				.output(.75f, AllItems.EXP_NUGGET.get());
		});
	}

	protected GeneratedRecipe rawOre(String metalName, Supplier<TagKey<Item>> input, Supplier<ItemLike> result, int xpMult) {
		return rawOre(metalName, input, result, false, xpMult);
	}

	protected GeneratedRecipe rawOreBlock(String metalName, Supplier<TagKey<Item>> input, Supplier<ItemLike> result, int xpMult) {
		return rawOre(metalName, input, result, true, xpMult);
	}

	protected GeneratedRecipe rawOre(String metalName, Supplier<TagKey<Item>> input, Supplier<ItemLike> result, boolean block, int xpMult) {
		return create("raw_" + metalName + (block ? "_block" : ""), b -> {
			int amount = block ? 9 : 1;
			return b.duration(400)
				.require(input.get())
				.output(result.get(), amount)
				.output(.75f, AllItems.EXP_NUGGET.get(), amount * xpMult);
		});
	}

	protected GeneratedRecipe moddedRawOre(CompatMetals metal, Supplier<ItemLike> result) {
		return moddedRawOre(metal, result, false);
	}

	protected GeneratedRecipe moddedRawOreBlock(CompatMetals metal, Supplier<ItemLike> result) {
		return moddedRawOre(metal, result, true);
	}

	protected GeneratedRecipe moddedRawOre(CompatMetals metal, Supplier<ItemLike> result, boolean block) {
		String name = metal.getName();
		return create("raw_" + name + (block ? "_block" : ""), b -> {
			int amount = block ? 9 : 1;
			String tagPath = (block ? "storage_blocks/raw_" : "raw_materials/") + name;
			return b.duration(400)
				.withCondition(new NotCondition(new TagEmptyCondition("forge", tagPath)))
				.require(AllTags.forgeItemTag(tagPath))
				.output(result.get(), amount)
				.output(.75f, AllItems.EXP_NUGGET.get(), amount);
		});
	}

	protected GeneratedRecipe sgStoneOres(String... types) {
		for (String type : types) {
			create(Mods.SILENT_GEMS.recipeId(type + "_ore"), b -> b.duration(350)
					.require(Mods.SILENT_GEMS, type + "_ore")
					.output(1f, Mods.SILENT_GEMS, type, 2)
					.output(.25f, Mods.SILENT_GEMS, type, 1)
					.output(.75f, AllItems.EXP_NUGGET.get())
					.output(.12f, Items.COBBLESTONE)
					.whenModLoaded(Mods.SILENT_GEMS.getId()));
		}
		return null;
	}

	protected GeneratedRecipe sgNetherOres(String... types) {
		for (String type : types) {
			create(Mods.SILENT_GEMS.recipeId(type + "_ore"), b -> b.duration(350)
					.require(Mods.SILENT_GEMS, type + "_ore")
					.output(1f, Mods.SILENT_GEMS, type, 2)
					.output(.25f, Mods.SILENT_GEMS, type, 1)
					.output(.75f, AllItems.EXP_NUGGET.get())
					.output(.12f, Items.NETHERRACK)
					.whenModLoaded(Mods.SILENT_GEMS.getId()));
		}
		return null;
	}

	protected GeneratedRecipe sgEndOres(String... types) {
		for (String type : types) {
			create(Mods.SILENT_GEMS.recipeId(type + "_ore"), b -> b.duration(350)
					.require(Mods.SILENT_GEMS, type + "_ore")
					.output(1f, Mods.SILENT_GEMS, type, 2)
					.output(.25f, Mods.SILENT_GEMS, type, 1)
					.output(.75f, AllItems.EXP_NUGGET.get())
					.output(.12f, Items.END_STONE)
					.whenModLoaded(Mods.SILENT_GEMS.getId()));
		}
		return null;
	}

	protected GeneratedRecipe sfPlants(String... types) {
		for (String type : types) {
			create(Mods.SF.recipeId(type), b -> b.duration(150)
					.require(Mods.SF, type)
					.output(1f, AllItems.WHEAT_FLOUR.get(), 1)
					.output(.25f, AllItems.WHEAT_FLOUR.get(), 2)
					.output(.25f, Mods.SF, type + "_seeds", 1)
					.whenModLoaded(Mods.SF.getId()));
		}
		return null;
	}

	protected GeneratedRecipe thOres(String... types) {
		for (String type : types) {
			create(Mods.TH.recipeId(type + "_ore"), b -> b.duration(350)
					.require(Mods.TH, type + "_ore")
					.output(1f, Mods.TH, type, 2)
					.output(.25f, Mods.TH, type, 1)
					.output(.12f, Items.COBBLESTONE)
					.output(.75f, AllItems.EXP_NUGGET.get())
					.whenModLoaded(Mods.TH.getId()));
		}
		return null;
	}

	protected GeneratedRecipe eoNetherOre(String material, ItemLike result, int count){
		String oreName = "ore_" + material + "_nether";
		return create(Mods.EO.recipeId(oreName), b -> b.duration(350)
				.require(Mods.EO, oreName)
				.output(1f, result, count)
				.output(.25f, result)
				.output(.75f, AllItems.EXP_NUGGET.get())
				.output(.12f, Items.NETHERRACK)
				.whenModLoaded(Mods.EO.getId()));
	}

	protected GeneratedRecipe eoEndOre(String material, ItemLike result, int count){
		String oreName = "ore_" + material + "_end";
		return create(Mods.EO.recipeId(oreName), b -> b.duration(350)
				.require(Mods.EO, oreName)
				.output(1f, result, count)
				.output(.25f, result)
				.output(.75f, AllItems.EXP_NUGGET.get())
				.output(.12f, Items.END_STONE)
				.whenModLoaded(Mods.EO.getId()));
	}

	protected GeneratedRecipe ensStones(String... stones) {
		for (String stone : stones) {
			String crushed = "crushed_" + stone;
			create(Mods.ENS.recipeId(stone), b -> b.duration(350)
					.require(Mods.MC, stone)
					.output(Mods.ENS, crushed)
					.whenModLoaded(Mods.ENS.getId()));
		}
		return null;
	}

	public CrushingRecipeGen(PackOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CRUSHING;
	}

}
