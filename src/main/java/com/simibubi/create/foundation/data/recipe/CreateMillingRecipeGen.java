package com.simibubi.create.foundation.data.recipe;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.MillingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.common.Tags;

/**
 * Create's own Data Generation for Milling recipes
 *
 * @see MillingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateMillingRecipeGen extends MillingRecipeGen {

	GeneratedRecipe

		GRANITE = create(() -> Blocks.GRANITE, b -> b.duration(200)
		.output(Blocks.RED_SAND)),

	WOOL = create("wool", b -> b.duration(100)
		.require(ItemTags.WOOL)
		.output(Items.STRING)),

	CLAY = create(() -> Blocks.CLAY, b -> b.duration(50)
		.output(Items.CLAY_BALL, 4)),

	CALCITE = create(() -> Items.CALCITE, b -> b.duration(250)
		.output(.75f, Items.BONE_MEAL, 1)),
		DRIPSTONE = create(() -> Items.DRIPSTONE_BLOCK, b -> b.duration(250)
			.output(Items.CLAY_BALL, 1)),

	TERRACOTTA = create(() -> Blocks.TERRACOTTA, b -> b.duration(200)
		.output(Blocks.RED_SAND)),
		ANDESITE = create(() -> Blocks.ANDESITE, b -> b.duration(200)
			.output(Blocks.COBBLESTONE)),
		COBBLESTONE = create(() -> Blocks.COBBLESTONE, b -> b.duration(250)
			.output(Blocks.GRAVEL)),
		GRAVEL = create(() -> Blocks.GRAVEL, b -> b.duration(250)
			.output(Items.FLINT)),
		SANDSTONE = create(() -> Blocks.SANDSTONE, b -> b.duration(150)
			.output(Blocks.SAND)),

	WHEAT = create(() -> Items.WHEAT, b -> b.duration(150)
		.output(AllItems.WHEAT_FLOUR.get())
		.output(.25f, AllItems.WHEAT_FLOUR.get(), 2)
		.output(.25f, Items.WHEAT_SEEDS)),

	BONE = create(() -> Items.BONE, b -> b.duration(100)
		.output(Items.BONE_MEAL, 3)
		.output(.25f, dye(DyeColor.WHITE), 1)
		.output(.25f, Items.BONE_MEAL, 3)),

	CACTUS = create(() -> Blocks.CACTUS, b -> b.duration(50)
		.output(dye(DyeColor.GREEN), 2)
		.output(.1f, dye(DyeColor.GREEN), 1)),

	SEA_PICKLE = create(() -> Blocks.SEA_PICKLE, b -> b.duration(50)
		.output(dye(DyeColor.LIME), 2)
		.output(.1f, dye(DyeColor.GREEN))),

	BONE_MEAL = create(() -> Items.BONE_MEAL, b -> b.duration(70)
		.output(dye(DyeColor.WHITE), 2)
		.output(.1f, dye(DyeColor.LIGHT_GRAY), 1)),

	COCOA_BEANS = create(() -> Items.COCOA_BEANS, b -> b.duration(70)
		.output(dye(DyeColor.BROWN), 2)
		.output(.1f, dye(DyeColor.BROWN), 1)),

	SADDLE = create(() -> Items.SADDLE, b -> b.duration(200)
		.output(Items.LEATHER, 2)
		.output(.5f, Items.LEATHER, 2)),

	SUGAR_CANE = create(() -> Items.SUGAR_CANE, b -> b.duration(50)
		.output(Items.SUGAR, 2)
		.output(.1f, Items.SUGAR)),

	BEETROOT = create(() -> Items.BEETROOT, b -> b.duration(70)
		.output(dye(DyeColor.RED), 2)
		.output(.1f, Items.BEETROOT_SEEDS)),

	INK_SAC = create(() -> Items.INK_SAC, b -> b.duration(100)
		.output(dye(DyeColor.BLACK), 2)
		.output(.1f, dye(DyeColor.GRAY))),

	CHARCOAL = create(() -> Items.CHARCOAL, b -> b.duration(100)
		.output(dye(DyeColor.BLACK), 1)
		.output(.1f, dye(DyeColor.GRAY), 2)),

	COAL = create(() -> Items.COAL, b -> b.duration(100)
		.output(dye(DyeColor.BLACK), 2)
		.output(.1f, dye(DyeColor.GRAY), 1)),

	LAPIS_LAZULI = create(() -> Items.LAPIS_LAZULI, b -> b.duration(100)
		.output(dye(DyeColor.BLUE), 2)
		.output(.1f, dye(DyeColor.BLUE))),

	AZURE_BLUET = create(() -> Blocks.AZURE_BLUET, b -> b.duration(50)
		.output(dye(DyeColor.LIGHT_GRAY), 2)
		.output(.1f, dye(DyeColor.WHITE), 2)),

	BLUE_ORCHID = create(() -> Blocks.BLUE_ORCHID, b -> b.duration(50)
		.output(dye(DyeColor.LIGHT_BLUE), 2)
		.output(.05f, dye(DyeColor.LIGHT_GRAY), 1)),

	FERN = create(() -> Blocks.FERN, b -> b.duration(50)
		.output(dye(DyeColor.GREEN))
		.output(.1f, Items.WHEAT_SEEDS)),

	LARGE_FERN = create(() -> Blocks.LARGE_FERN, b -> b.duration(50)
		.output(dye(DyeColor.GREEN), 2)
		.output(.5f, dye(DyeColor.GREEN))
		.output(.1f, Items.WHEAT_SEEDS)),

	LILAC = create(() -> Blocks.LILAC, b -> b.duration(100)
		.output(dye(DyeColor.MAGENTA), 3)
		.output(.25f, dye(DyeColor.MAGENTA))
		.output(.25f, dye(DyeColor.PURPLE))),

	PEONY = create(() -> Blocks.PEONY, b -> b.duration(100)
		.output(dye(DyeColor.PINK), 3)
		.output(.25f, dye(DyeColor.MAGENTA))
		.output(.25f, dye(DyeColor.PINK))),

	ALLIUM = create(() -> Blocks.ALLIUM, b -> b.duration(50)
		.output(dye(DyeColor.MAGENTA), 2)
		.output(.1f, dye(DyeColor.PURPLE), 2)
		.output(.1f, dye(DyeColor.PINK))),

	LILY_OF_THE_VALLEY = create(() -> Blocks.LILY_OF_THE_VALLEY, b -> b.duration(50)
		.output(dye(DyeColor.WHITE), 2)
		.output(.1f, dye(DyeColor.LIME))
		.output(.1f, dye(DyeColor.WHITE))),

	ROSE_BUSH = create(() -> Blocks.ROSE_BUSH, b -> b.duration(50)
		.output(dye(DyeColor.RED), 3)
		.output(.05f, dye(DyeColor.GREEN), 2)
		.output(.25f, dye(DyeColor.RED), 2)),

	SUNFLOWER = create(() -> Blocks.SUNFLOWER, b -> b.duration(100)
		.output(dye(DyeColor.YELLOW), 3)
		.output(.25f, dye(DyeColor.ORANGE))
		.output(.25f, dye(DyeColor.YELLOW))),

	OXEYE_DAISY = create(() -> Blocks.OXEYE_DAISY, b -> b.duration(50)
		.output(dye(DyeColor.LIGHT_GRAY), 2)
		.output(.2f, dye(DyeColor.WHITE))
		.output(.05f, dye(DyeColor.YELLOW))),

	POPPY = create(() -> Blocks.POPPY, b -> b.duration(50)
		.output(dye(DyeColor.RED), 2)
		.output(.05f, dye(DyeColor.GREEN))),

	DANDELION = create(() -> Blocks.DANDELION, b -> b.duration(50)
		.output(dye(DyeColor.YELLOW), 2)
		.output(.05f, dye(DyeColor.YELLOW))),

	CORNFLOWER = create(() -> Blocks.CORNFLOWER, b -> b.duration(50)
		.output(dye(DyeColor.BLUE), 2)),

	WITHER_ROSE = create(() -> Blocks.WITHER_ROSE, b -> b.duration(50)
		.output(dye(DyeColor.BLACK), 2)
		.output(.1f, dye(DyeColor.BLACK))),

	ORANGE_TULIP = create(() -> Blocks.ORANGE_TULIP, b -> b.duration(50)
		.output(dye(DyeColor.ORANGE), 2)
		.output(.1f, dye(DyeColor.LIME))),

	RED_TULIP = create(() -> Blocks.RED_TULIP, b -> b.duration(50)
		.output(dye(DyeColor.RED), 2)
		.output(.1f, dye(DyeColor.LIME))),

	WHITE_TULIP = create(() -> Blocks.WHITE_TULIP, b -> b.duration(50)
		.output(dye(DyeColor.WHITE), 2)
		.output(.1f, dye(DyeColor.LIME))),

	PINK_TULIP = create(() -> Blocks.PINK_TULIP, b -> b.duration(50)
		.output(dye(DyeColor.PINK), 2)
		.output(.1f, dye(DyeColor.LIME))),

	PINK_PETALS = create(() -> Blocks.PINK_PETALS, b -> b.duration(50)
		.output(dye(DyeColor.PINK), 2)
		.output(.1f, dye(DyeColor.LIME))),

	PITCHER_PLANT = create(() -> Blocks.PITCHER_PLANT, b -> b.duration(50)
		.output(dye(DyeColor.CYAN), 4)
		.output(.1f, dye(DyeColor.PURPLE))),

	TORCHFLOWER = create(() -> Blocks.TORCHFLOWER, b -> b.duration(50)
		.output(dye(DyeColor.ORANGE), 2)
		.output(.1f, dye(DyeColor.GREEN))),

	TALL_GRASS = create(() -> Blocks.TALL_GRASS, b -> b.duration(100)
		.output(.5f, Items.WHEAT_SEEDS)),
		GRASS = create(() -> Blocks.SHORT_GRASS, b -> b.duration(50)
			.output(.25f, Items.WHEAT_SEEDS)),

	// AE2

	AE2_CERTUS = create(Mods.AE2.recipeId("certus_quartz"), b -> b.duration(200)
		.require(AllItemTags.CERTUS_QUARTZ.tag)
		.output(Mods.AE2, "certus_quartz_dust")
		.whenModLoaded(Mods.AE2.getId())),

	AE2_ENDER = create(Mods.AE2.recipeId("ender_pearl"), b -> b.duration(100)
		.require(Tags.Items.ENDER_PEARLS)
		.output(Mods.AE2, "ender_dust")
		.whenModLoaded(Mods.AE2.getId())),

	AE2_FLUIX = create(Mods.AE2.recipeId("fluix_crystal"), b -> b.duration(200)
		.require(Mods.AE2, "fluix_crystal")
		.output(Mods.AE2, "fluix_dust")
		.whenModLoaded(Mods.AE2.getId())),

	AE2_SKY_STONE = create(Mods.AE2.recipeId("sky_stone_block"), b -> b.duration(300)
		.require(Mods.AE2, "sky_stone_block")
		.output(Mods.AE2, "sky_dust")
		.whenModLoaded(Mods.AE2.getId())),

	// Atmospheric

	ATMO_GILIA = create(Mods.ATM.recipeId("gilia"), b -> b.duration(50)
		.require(Mods.ATM, "gilia")
		.output(dye(DyeColor.PURPLE), 2)
		.output(.1f, dye(DyeColor.MAGENTA), 2)
		.output(.1f, dye(DyeColor.PINK))
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_HOT_BRUSH = create(Mods.ATM.recipeId("hot_monkey_brush"), b -> b.duration(50)
		.require(Mods.ATM, "hot_monkey_brush")
		.output(dye(DyeColor.ORANGE), 2)
		.output(.05f, dye(DyeColor.RED))
		.output(.05f, dye(DyeColor.YELLOW))
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_SCALDING_BRUSH = create(Mods.ATM.recipeId("scalding_monkey_brush"), b -> b.duration(50)
		.require(Mods.ATM, "scalding_monkey_brush")
		.output(dye(DyeColor.RED), 2)
		.output(.1f, dye(DyeColor.RED), 2)
		.output(.1f, dye(DyeColor.ORANGE))
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_WARM_BRUSH = create(Mods.ATM.recipeId("warm_monkey_brush"), b -> b.duration(50)
		.require(Mods.ATM, "warm_monkey_brush")
		.output(dye(DyeColor.YELLOW), 2)
		.output(.1f, dye(DyeColor.YELLOW), 2)
		.output(.1f, dye(DyeColor.ORANGE))
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_YUCCA_FLOWER = create(Mods.ATM.recipeId("yucca_flower"), b -> b.duration(50)
		.require(Mods.ATM, "yucca_flower")
		.output(dye(DyeColor.LIGHT_GRAY), 2)
		.output(.05f, dye(DyeColor.WHITE))
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_TALL_YUCCA_FLOWER = create(Mods.ATM.recipeId("tall_yucca_flower"), b -> b.duration(50)
		.require(Mods.ATM, "tall_yucca_flower")
		.output(dye(DyeColor.LIGHT_GRAY), 3)
		.output(0.25f, dye(DyeColor.LIGHT_GRAY), 2)
		.output(.05f, dye(DyeColor.WHITE), 2)
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_FIRETHORN = create(Mods.ATM.recipeId("firethorn"), b -> b.duration(50)
		.require(Mods.ATM, "firethorn")
		.output(dye(DyeColor.RED), 2)
		.output(.1f, dye(DyeColor.ORANGE), 2)
		.output(.1f, dye(DyeColor.GREEN))
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_FORSYTHIA = create(Mods.ATM.recipeId("forsythia"), b -> b.duration(50)
		.require(Mods.ATM, "forsythia")
		.output(dye(DyeColor.YELLOW), 2)
		.output(.1f, dye(DyeColor.LIME), 2)
		.output(.1f, dye(DyeColor.YELLOW))
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_CACTUS = create(Mods.ATM.recipeId("barrel_cactus"), b -> b.duration(50)
		.require(Mods.ATM, "barrel_cactus")
		.output(dye(DyeColor.ORANGE), 2)
		.output(.1f, dye(DyeColor.GREEN), 3)
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_HYACINTH = create(Mods.ATM.recipeId("water_hyacinth"), b -> b.duration(50)
		.require(Mods.ATM, "water_hyacinth")
		.output(dye(DyeColor.PURPLE), 3)
		.output(0.25f, dye(DyeColor.LIME), 2)
		.output(.05f, dye(DyeColor.BROWN), 2)
		.whenModLoaded(Mods.ATM.getId())),

	ATMO_SAND_1 = moddedSandstone(Mods.ATM, "arid"),
	ATMO_SAND_2 = moddedSandstone(Mods.ATM, "red_arid"),

	// Autumnity

	AUTUM_CROCUS = create(Mods.AUTUM.recipeId("autumn_crocus"), b -> b.duration(50)
		.require(Mods.AUTUM, "autumn_crocus")
		.output(dye(DyeColor.MAGENTA), 2)
		.output(.1f, dye(DyeColor.PINK), 2)
		.output(.1f, dye(DyeColor.PURPLE))
		.whenModLoaded(Mods.AUTUM.getId())),

	// Biomes O' Plenty
	BOP_HYDRANGEA = bopFlower("blue_hydrangea", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.LIGHT_BLUE), dye(DyeColor.GREEN), dye(DyeColor.LIGHT_BLUE)), List.of(3, 2, 2)),

	BOP_GOLDENROD = bopFlower("goldenrod", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.YELLOW), dye(DyeColor.YELLOW), dye(DyeColor.GREEN)), List.of(3, 2, 2)),

	BOP_BLOSSOM = bopFlower("burning_blossom", List.of(1f, .1f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.LIME)), List.of(2, 1)),

	BOP_GLOWFLOWER = bopFlower("glowflower", List.of(1f, .1f),
		List.of(dye(DyeColor.CYAN), dye(DyeColor.WHITE)), List.of(2, 1)),

	BOP_LAVENDER = bopFlower("lavender", List.of(1f, .05f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.GREEN)), List.of(2, 1)),

	BOP_TALL_LAVENDER = bopFlower("tall_lavender", List.of(1f, 0.25f, .05f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.PURPLE), dye(DyeColor.GREEN)), List.of(3, 2, 2)),

	BOP_WHITE_LAVENDER = bopFlower("white_lavender", List.of(1f, .05f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.GREEN)), List.of(2, 1)),

	BOP_TALL_WHITE_LAVENDER = bopFlower("tall_white_lavender", List.of(1f, 0.25f, .05f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.LIGHT_BLUE), dye(DyeColor.GREEN)), List.of(3, 2, 2)),

	BOP_COSMOS = bopFlower("orange_cosmos", List.of(1f, .1f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.LIME)), List.of(2, 1)),

	BOP_DAFFODIL = bopFlower("pink_daffodil", List.of(1f, .25f, .05f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.MAGENTA), dye(DyeColor.CYAN)), List.of(2, 1, 1)),

	BOP_HIBISCUS = bopFlower("pink_hibiscus", List.of(1f, .25f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.YELLOW), dye(DyeColor.GREEN)), List.of(2, 1, 1)),

	BOP_ROSE = bopFlower("rose", List.of(1f, .05f),
		List.of(dye(DyeColor.RED), dye(DyeColor.GREEN)), List.of(2, 1)),

	BOP_VIOLET = bopFlower("violet", 1f, dye(DyeColor.PURPLE), 2),

	BOP_WILDFLOWER = bopFlower("wildflower", List.of(1f, .1f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.LIME)), List.of(2, 1)),

	BOP_PETALS = bopFlower("white_petals", 1f, dye(DyeColor.WHITE), 2),

	BOP_IRIS = bopFlower("icy_iris", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.LIGHT_BLUE), dye(DyeColor.LIGHT_GRAY), dye(DyeColor.LIGHT_BLUE)), List.of(3, 2, 2)),

	BOP_LILY = bopFlower("wilted_lily", 1f, dye(DyeColor.GRAY), 2),

	BOP_ENDBLOOM = bopFlower("endbloom", 1f, dye(DyeColor.LIGHT_GRAY), 2),

	BOP_WATERLILY = bopFlower("waterlily", List.of(1f, .05f),
		List.of(dye(DyeColor.RED), dye(DyeColor.PINK)), List.of(2, 1)),

	BOP_CACTUS = bopFlower("tiny_cactus", List.of(1f, 0.1f),
		List.of(dye(DyeColor.GREEN), dye(DyeColor.GREEN)), List.of(2, 1)),

	BOP_CATTAIL = bopFlower("cattail", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.BROWN), dye(DyeColor.GREEN), dye(DyeColor.BROWN)), List.of(3, 2, 2)),

	BOP_SAND_1 = moddedSandstone(Mods.BOP, "white"),
	BOP_SAND_2 = moddedSandstone(Mods.BOP, "orange"),
	BOP_SAND_3 = moddedSandstone(Mods.BOP, "black"),

	// Botania
	BTN_PETALS = botaniaPetals("black", "blue", "brown", "cyan", "gray", "green", "light_blue",
		"light_gray", "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow"),

	// Buzzier Bees

	BB_BUTTERCUP = create(Mods.BB.recipeId("buttercup"), b -> b.duration(50)
		.require(Mods.BB, "buttercup")
		.output(dye(DyeColor.YELLOW), 2)
		.output(.1f, dye(DyeColor.LIME))
		.whenModLoaded(Mods.BB.getId())),

	BB_PINK_CLOVER = create(Mods.BB.recipeId("pink_clover"), b -> b.duration(50)
		.require(Mods.BB, "pink_clover")
		.output(dye(DyeColor.PINK), 2)
		.output(.1f, dye(DyeColor.LIME))
		.whenModLoaded(Mods.BB.getId())),

	BB_WHITE_CLOVER = create(Mods.BB.recipeId("white_clover"), b -> b.duration(50)
		.require(Mods.BB, "white_clover")
		.output(dye(DyeColor.WHITE), 2)
		.output(.1f, dye(DyeColor.LIME))
		.whenModLoaded(Mods.BB.getId())),

	// Oh The Biomes We've Gone

	BWG_ALLIUM_BUSH = bwgFlower("allium_flower_bush", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.GREEN), dye(DyeColor.MAGENTA)), List.of(3, 2, 2)),

	BWG_BELLFLOWER = bwgFlower("alpine_bellflower", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.BLUE), dye(DyeColor.GREEN)), List.of(2, 2, 1)),

	BWG_AMARANTH = bwgFlower("amaranth", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.RED), dye(DyeColor.GREEN), dye(DyeColor.RED)), List.of(3, 2, 2)),

	BWG_ANGELICA = bwgFlower("angelica", List.of(1f, .1f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_BEGONIA = bwgFlower("begonia", List.of(1f, .1f),
		List.of(dye(DyeColor.RED), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_BISTORT = bwgFlower("bistort", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.RED), dye(DyeColor.GREEN)), List.of(2, 2, 1)),

	BWG_BLACK_ROSE = bwgFlower("black_rose", List.of(1f, .1f),
		List.of(dye(DyeColor.BLACK), dye(DyeColor.BLACK)), List.of(2, 1)),

	BWG_BLUE_SAGE = bwgFlower("blue_sage", List.of(1f, .1f),
		List.of(dye(DyeColor.BLUE), dye(DyeColor.CYAN)), List.of(2, 1)),

	BWG_CALIFORNIA_POPPY = bwgFlower("california_poppy", List.of(1f, .05f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_CROCUS = bwgFlower("crocus", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.BLUE), dye(DyeColor.GREEN)), List.of(2, 2, 1)),

	BWG_CYAN_AMARANTH = bwgFlower("cyan_amaranth", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.CYAN), dye(DyeColor.GREEN), dye(DyeColor.CYAN)), List.of(3, 2, 2)),

	BWG_CYAN_ROSE = bwgFlower("cyan_rose", List.of(1f, .1f),
		List.of(dye(DyeColor.CYAN), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_CYAN_TULIP = bwgFlower("cyan_tulip", List.of(1f, .1f),
		List.of(dye(DyeColor.CYAN), dye(DyeColor.LIME)), List.of(2, 1)),

	BWG_DAFFODIL = bwgFlower("daffodil", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.GREEN), dye(DyeColor.MAGENTA)), List.of(2, 1, 1)),

	BWG_DELPHINIUM = bwgFlower("delphinium", List.of(1f, .1f),
		List.of(dye(DyeColor.BLUE), dye(DyeColor.BLUE)), List.of(3, 1)),

	BWG_FAIRY_SLIPPER = bwgFlower("fairy_slipper", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.PINK), dye(DyeColor.YELLOW)), List.of(2, 2, 1)),

	BWG_FIRECRACKER_BUSH = bwgFlower("firecracker_flower_bush", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.GREEN), dye(DyeColor.RED)), List.of(3, 2, 2)),

	BWG_FOXGLOVE = bwgFlower("foxglove", List.of(1f, .25f, .25f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.PINK), dye(DyeColor.YELLOW)), List.of(2, 1, 1)),

	BWG_GREEN_TULIP = bwgFlower("green_tulip", List.of(1f, .1f),
		List.of(dye(DyeColor.LIME), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_GUZMANIA = bwgFlower("guzmania", List.of(1f, .25f, .25f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.PINK), dye(DyeColor.YELLOW)), List.of(2, 1, 1)),

	BWG_HYDRANGEA = bwgFlower("hydrangea_bush", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.BLUE), dye(DyeColor.WHITE)), List.of(2, 2, 1)),

	BWG_INCAN_LILY = bwgFlower("incan_lily", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.GREEN), dye(DyeColor.RED)), List.of(2, 1, 1)),

	BWG_IRIS = bwgFlower("iris", List.of(1f, .05f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_ORCHID = bwgFlower("japanese_orchid", List.of(1f, .05f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.WHITE)), List.of(2, 1)),

	BWG_PURPLE_SAGE = bwgFlower("purple_sage", List.of(1f, .1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.MAGENTA)), List.of(2, 1)),

	BWG_KOVAN = bwgFlower("kovan_flower", List.of(1f, .2f, .05f),
		List.of(dye(DyeColor.RED), dye(DyeColor.LIME), dye(DyeColor.GREEN)), List.of(2, 1, 1)),

	BWG_LAZARUS_BELLFLOWER = bwgFlower("lazarus_bellflower", List.of(1f, .1f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_LOLLIPOP = bwgFlower("lollipop_flower", List.of(1f, .25f, .05f),
		List.of(dye(DyeColor.YELLOW), dye(DyeColor.YELLOW), dye(DyeColor.GREEN)), List.of(2, 1, 1)),

	BWG_MAGENTA_AMARANTH = bwgFlower("magenta_amaranth", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.GREEN), dye(DyeColor.MAGENTA)), List.of(3, 2, 2)),

	BWG_MAGENTA_TULIP = bwgFlower("magenta_tulip", List.of(1f, .1f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.LIME)), List.of(2, 1)),

	BWG_ORANGE_AMARANTH = bwgFlower("orange_amaranth", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.GREEN), dye(DyeColor.ORANGE)), List.of(3, 2, 2)),

	BWG_DAISY = bwgFlower("orange_daisy", List.of(1f, .2f, .05f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.YELLOW), dye(DyeColor.LIME)), List.of(2, 1, 1)),

	BWG_OSIRIA_ROSE = bwgFlower("osiria_rose", List.of(1f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_PEACH_LEATHER = bwgFlower("peach_leather_flower", List.of(1f, .25f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_PINK_ALLIUM = bwgFlower("pink_allium", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.PINK), dye(DyeColor.PURPLE)), List.of(2, 2, 1)),

	BWG_PINK_ALLIUM_BUSH = bwgFlower("pink_allium_flower_bush", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.GREEN), dye(DyeColor.MAGENTA)), List.of(3, 2, 2)),

	BWG_PINK_ANEMONE = bwgFlower("pink_anemone", List.of(1f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.PURPLE)), List.of(2, 2)),

	BWG_PINK_DAFODIL = bwgFlower("pink_daffodil", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.GREEN), dye(DyeColor.WHITE)), List.of(2, 1, 1)),

	BWG_PROTEA = bwgFlower("protea_flower", List.of(1f, .1f, .05f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.LIME), dye(DyeColor.PURPLE)), List.of(2, 1, 1)),

	BWG_PURPLE_AMARANTH = bwgFlower("purple_amaranth", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.GREEN), dye(DyeColor.PURPLE)), List.of(3, 2, 2)),

	BWG_PURPLE_TULIP = bwgFlower("purple_tulip", List.of(1f, .1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.LIME)), List.of(2, 1)),

	BWG_RICHEA = bwgFlower("richea", List.of(1f, .1f, .05f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.PINK), dye(DyeColor.YELLOW)), List.of(2, 1, 1)),

	BWG_ROSE = bwgFlower("rose", List.of(1f, .1f),
		List.of(dye(DyeColor.RED), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_SILVER_VASE = bwgFlower("silver_vase_flower", List.of(1f, .1f, .05f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.GREEN), dye(DyeColor.WHITE)), List.of(2, 1, 1)),

	BWG_SNOWDROPS = bwgFlower("snowdrops", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.LIME), dye(DyeColor.WHITE)), List.of(2, 1, 1)),

	BWG_TALL_ALLIUM = bwgFlower("tall_allium", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.PURPLE), dye(DyeColor.MAGENTA)), List.of(3, 2, 2)),

	BWG_TALL_PINK_ALLIUM = bwgFlower("tall_pink_allium", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.PINK), dye(DyeColor.MAGENTA)), List.of(3, 2, 2)),

	BWG_VIOLET_LEATHER = bwgFlower("violet_leather_flower", List.of(1f, .25f),
		List.of(dye(DyeColor.BLUE), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_WHITE_ANEMONE = bwgFlower("white_anemone", List.of(1f, .1f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.LIGHT_GRAY)), List.of(2, 2)),

	BWG_PUFFBALL = create(Mods.BWG.recipeId("white_puffball_cap"), b -> b.duration(150)
		.require(Mods.BWG, "white_puffball_cap")
		.output(.25f, Mods.BWG, "white_puffball_spores", 1)
		.whenModLoaded(Mods.BWG.getId())),

	BWG_WHITE_SAGE = bwgFlower("white_sage", List.of(1f, .1f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.GRAY)), List.of(2, 1)),

	BWG_WINTER_CYCLAMEN = bwgFlower("winter_cyclamen", List.of(1f, .1f),
		List.of(dye(DyeColor.CYAN), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_WINTER_ROSE = bwgFlower("winter_rose", List.of(1f, .1f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_WINTER_SCILLA = bwgFlower("winter_scilla", List.of(1f, .1f),
		List.of(dye(DyeColor.LIGHT_BLUE), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_YELLOW_DAFFODIL = bwgFlower("yellow_daffodil", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.YELLOW), dye(DyeColor.GREEN), dye(DyeColor.PINK)), List.of(2, 1, 1)),

	BWG_YELLOW_TULIP = bwgFlower("yellow_tulip", List.of(1f, .1f),
		List.of(dye(DyeColor.YELLOW), dye(DyeColor.LIME)), List.of(2, 1)),

	BWG_WHITE_ALLIUM = bwgFlower("white_allium", List.of(1f, .1f, .1f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.LIGHT_GRAY), dye(DyeColor.GRAY)), List.of(2, 2, 1)),

	BWG_TALL_WHITE_ALLIUM = bwgFlower("tall_white_allium", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.WHITE), dye(DyeColor.LIGHT_GRAY)), List.of(3, 2, 2)),

	BWG_WHITE_ALLIUM_BUSH = bwgFlower("white_allium_flower_bush", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.GREEN), dye(DyeColor.LIGHT_GRAY)), List.of(3, 2, 2)),

	BWG_BLUE_ROSE_BUSH = bwgFlower("blue_rose_bush", List.of(1f, .05f, .25f),
		List.of(dye(DyeColor.BLUE), dye(DyeColor.GREEN), dye(DyeColor.BLUE)), List.of(3, 2, 2)),

	BWG_HORSEWEED = bwgFlower("horseweed", List.of(1f, 0.25f),
		List.of(dye(DyeColor.GREEN), dye(DyeColor.BROWN)), List.of(2, 1)),

	BWG_WINTER_SUCCULENT = bwgFlower("winter_succulent", List.of(1f, 0.25f),
		List.of(dye(DyeColor.GREEN), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_MINI_CACTUS = bwgFlower("mini_cactus", List.of(1f),
		List.of(dye(DyeColor.GREEN)), List.of(2)),

	BWG_PRICKLY_PEAR_CACTUS = bwgFlower("prickly_pear_cactus", List.of(1f, 0.25f),
		List.of(dye(DyeColor.GREEN), dye(DyeColor.GREEN)), List.of(2, 1)),

	BWG_GOLDEN_SPINED_CACTUS = bwgFlower("golden_spined_cactus", List.of(1f, 0.25f),
		List.of(dye(DyeColor.GREEN), dye(DyeColor.YELLOW)), List.of(2, 1)),

	BWG_SAND_1 = moddedSandstone(Mods.BWG, "black"),
	BWG_SAND_2 = moddedSandstone(Mods.BWG, "white"),
	BWG_SAND_3 = moddedSandstone(Mods.BWG, "blue"),
	BWG_SAND_4 = moddedSandstone(Mods.BWG, "purple"),
	BWG_SAND_5 = moddedSandstone(Mods.BWG, "pink"),
	BWG_SAND_6 = moddedSandstone(Mods.BWG, "windswept"),

	// Environmental

	ENV_BIRD_OF_PARADISE = envFlower("bird_of_paradise", List.of(1f, .25f, .25f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.BLUE), dye(DyeColor.RED)), List.of(3, 1, 1)),

	ENV_BLUE_DELPHINIUM = envFlower("blue_delphinium", List.of(1f,.1f),
		List.of(dye(DyeColor.LIGHT_BLUE), dye(DyeColor.LIGHT_BLUE)), List.of(3,1)),

	ENV_BLUEBELL = envFlower("bluebell", List.of(1f),
		List.of(dye(DyeColor.BLUE)), List.of(2)),

	ENV_CARTWHEEL = envFlower("cartwheel", List.of(1f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.ORANGE)), List.of(2, 1)),

	ENV_DIANTHUS = envFlower("dianthus", List.of(1f,.1f),
		List.of(dye(DyeColor.LIME), dye(DyeColor.LIME)), List.of(2,1)),

	ENV_MAGENTA_HIBISCUS = envFlower("magenta_hibiscus", List.of(1f, .1f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.MAGENTA)), List.of(2, 1)),

	ENV_ORANGE_HIBISCUS = envFlower("orange_hibiscus", List.of(1f, .1f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.ORANGE)), List.of(2, 1)),

	ENV_PINK_DELPHINIUM = envFlower("pink_delphinium", List.of(1f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.PINK)), List.of(3, 1)),

	ENV_PINK_HIBISCUS = envFlower("pink_hibiscus", List.of(1f, .1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.PINK)), List.of(2, 1)),

	ENV_PURPLE_DELPHINIUM = envFlower("purple_delphinium", List.of(1f, .1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.PURPLE)), List.of(3, 1)),

	ENV_PURPLE_HIBISCUS = envFlower("purple_hibiscus", List.of(1f, .1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.PURPLE)), List.of(2, 1)),

	ENV_RED_HIBISCUS = envFlower("red_hibiscus", List.of(1f, .1f),
		List.of(dye(DyeColor.RED), dye(DyeColor.RED)), List.of(2, 1)),

	ENV_RED_LOTUS = envFlower("red_lotus_flower", List.of(1f, .1f),
		List.of(dye(DyeColor.RED), dye(DyeColor.RED)), List.of(2, 1)),

	ENV_VIOLET = envFlower("violet", List.of(1f, .1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.PURPLE)), List.of(2, 1)),

	ENV_WHITE_DELPHINIUM = envFlower("white_delphinium", List.of(1f, .1f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.WHITE)), List.of(3, 1)),

	ENV_WHITE_LOTUS_FLOWER = envFlower("white_lotus_flower", List.of(1f,.1f),
		List.of(dye(DyeColor.WHITE), dye(DyeColor.WHITE)), List.of(2,1)),

	ENV_YELLOW_HIBISCUS = envFlower("yellow_hibiscus", List.of(1f, .1f),
		List.of(dye(DyeColor.YELLOW), dye(DyeColor.YELLOW)), List.of(2, 1)),

	ENV_TASSELFLOWER = envFlower("tasselflower", List.of(1f, .1f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.GREEN)), List.of(2,1)),

	// Duidcraft
	DC_LAVENDER = create(Mods.DRUIDCRAFT.recipeId("lavender"), b -> b.duration(50)
		.require(Mods.DRUIDCRAFT, "lavender")
		.output(dye(DyeColor.PURPLE), 2)
		.output(.1f, dye(DyeColor.PURPLE))
		.whenModLoaded(Mods.DRUIDCRAFT.getId())),

	// Supplementaries
	SUP_FLAX = create(Mods.SUP.recipeId("flax"), b -> b.duration(150)
		.require(Mods.SUP, "flax")
		.output(Items.STRING)
		.output(.25f, Items.STRING, 2)
		.output(.25f, Mods.SUP, "flax_seeds", 1)
		.whenModLoaded(Mods.SUP.getId())),

	// Tinkers' Construct
	TIC_NERCOTIC_BONE = create(Mods.TIC.recipeId("nercotic_bone"), b -> b.duration(100)
		.require(Mods.TIC, "necrotic_bone")
		.output(Items.BONE_MEAL, 3)
		.output(.25f, dye(DyeColor.BLACK))
		.output(.25f, Items.BONE_MEAL, 3)
		.whenModLoaded(Mods.TIC.getId())),

	// Upgrade Aquatic

	UA_FLOWERING_RUSH = create(Mods.UA.recipeId("flowering_rush"), b -> b.duration(50)
		.require(Mods.UA, "flowering_rush")
		.output(dye(DyeColor.PINK), 3)
		.output(.25f, dye(DyeColor.PINK), 2)
		.whenModLoaded(Mods.UA.getId())),

	UA_PINK_SEAROCKET = create(Mods.UA.recipeId("pink_searocket"), b -> b.duration(50)
		.require(Mods.UA, "pink_searocket")
		.output(dye(DyeColor.PINK), 2)
		.output(.1f, dye(DyeColor.GREEN))
		.whenModLoaded(Mods.UA.getId())),

	UA_WHITE_SEAROCKET = create(Mods.UA.recipeId("white_searocket"), b -> b.duration(50)
		.require(Mods.UA, "white_searocket")
		.output(dye(DyeColor.WHITE), 2)
		.output(.1f, dye(DyeColor.GREEN))
		.whenModLoaded(Mods.UA.getId())),

	// Regions Unexplored

	RU_ALPHA_DANDELION = ruFlower("alpha_dandelion", List.of(1f, 0.05f),
		List.of(dye(DyeColor.YELLOW), dye(DyeColor.YELLOW)), List.of(2, 1)),

	RU_ALPHA_ROSE = ruFlower("alpha_rose", List.of(1f, 0.05f),
		List.of(dye(DyeColor.RED), dye(DyeColor.RED)), List.of(2, 1)),

	RU_ASTER = ruFlower("aster", List.of(1f, 0.2f, 0.05f),
		List.of(dye(DyeColor.LIGHT_BLUE), dye(DyeColor.WHITE), dye(DyeColor.LIGHT_GRAY)), List.of(2, 1, 1)),

	RU_BLACK_SNOWBELLE = ruFlower("black_snowbelle", List.of(1f),
		List.of(dye(DyeColor.BLACK)), List.of(2)),

	RU_BLEEDING_HEART = ruFlower("bleeding_heart", List.of(1f, 0.1f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.PINK)), List.of(2, 1)),

	RU_BLUE_LUPINE = ruFlower("blue_lupine", List.of(1f),
		List.of(dye(DyeColor.BLUE)), List.of(2)),

	RU_BLUE_SNOWBELLE = ruFlower("blue_snowbelle", List.of(1f),
		List.of(dye(DyeColor.BLUE)), List.of(2)),

	RU_BROWN_SNOWBELLE = ruFlower("brown_snowbelle", List.of(1f),
		List.of(dye(DyeColor.BROWN)), List.of(2)),

	RU_CACTUS_FLOWER = ruFlower("cactus_flower", List.of(1f, 0.2f, 0.1f),
		List.of(dye(DyeColor.MAGENTA), dye(DyeColor.PURPLE), dye(DyeColor.GREEN)), List.of(2, 1, 1)),

	RU_CYAN_SNOWBELLE = ruFlower("cyan_snowbelle", List.of(1f),
		List.of(dye(DyeColor.CYAN)), List.of(2)),

	RU_DAISY = ruFlower("daisy", List.of(1f, 0.2f, 0.05f),
		List.of(dye(DyeColor.LIGHT_GRAY), dye(DyeColor.WHITE), dye(DyeColor.YELLOW)), List.of(2, 1, 1)),

	RU_DAY_LILY = ruFlower("day_lily", List.of(1f, 0.1f, 0.1f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.LIME), dye(DyeColor.RED)), List.of(2, 1, 1)),

	RU_DORCEL = ruFlower("dorcel", List.of(1f, 0.1f),
		List.of(dye(DyeColor.BLACK), dye(DyeColor.BROWN)), List.of(2, 1)),

	RU_FELICIA_DAISY = ruFlower("felicia_daisy", List.of(1f, 0.2f, 0.05f),
		List.of(dye(DyeColor.LIGHT_BLUE), dye(DyeColor.BLUE), dye(DyeColor.WHITE)), List.of(2, 1, 1)),

	RU_FIREWEED = ruFlower("fireweed", List.of(1f),
		List.of(dye(DyeColor.MAGENTA)), List.of(2)),

	RU_GLITERING_BLOOM = ruFlower("glistering_bloom", List.of(1f, 0.25f, 0.25f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.MAGENTA), dye(DyeColor.LIGHT_BLUE)), List.of(2, 1, 1)),

	RU_GRAY_SNOWBELLE = ruFlower("gray_snowbelle", List.of(1f),
		List.of(dye(DyeColor.GRAY)), List.of(2)),

	RU_GREEN_SNOWBELLE = ruFlower("green_snowbelle", List.of(1f),
		List.of(dye(DyeColor.GREEN)), List.of(2)),

	RU_HIBISCUS = ruFlower("hibiscus", List.of(1f, 0.2f),
		List.of(dye(DyeColor.YELLOW), dye(DyeColor.RED)), List.of(2, 1)),

	RU_HYSSOP = ruFlower("hyssop", List.of(1f, 0.1f, 0.1f),
		List.of(dye(DyeColor.PURPLE), dye(DyeColor.MAGENTA), dye(DyeColor.GREEN)), List.of(2, 1, 1)),

	RU_LIGHT_BLUE_SNOWBELLE = ruFlower("light_blue_snowbelle", List.of(1f),
		List.of(dye(DyeColor.LIGHT_BLUE)), List.of(2)),

	RU_LIGHT_GRAY_SNOWBELLE = ruFlower("light_gray_snowbelle", List.of(1f),
		List.of(dye(DyeColor.LIGHT_GRAY)), List.of(2)),

	RU_LIME_SNOWBELLE = ruFlower("lime_snowbelle", List.of(1f),
		List.of(dye(DyeColor.LIME)), List.of(2)),

	RU_MAGENTA_SNOWBELLE = ruFlower("magenta_snowbelle", List.of(1f),
		List.of(dye(DyeColor.MAGENTA)), List.of(2)),

	RU_MALLOW = ruFlower("mallow", List.of(1f, 0.1f),
		List.of(dye(DyeColor.ORANGE), dye(DyeColor.LIME)), List.of(2, 1)),

	RU_ORANGE_CONEFLOWER = ruFlower("orange_coneflower", List.of(1f),
		List.of(dye(DyeColor.ORANGE)), List.of(2)),

	RU_ORANGE_SNOWBELLE = ruFlower("orange_snowbelle", List.of(1f),
		List.of(dye(DyeColor.ORANGE)), List.of(2)),

	RU_PINK_LUPINE = ruFlower("pink_lupine", List.of(1f),
		List.of(dye(DyeColor.PINK)), List.of(2)),

	RU_PINK_SNOWBELLE = ruFlower("pink_snowbelle", List.of(1f),
		List.of(dye(DyeColor.PINK)), List.of(2)),

	RU_POPPY_BUSH = ruFlower("poppy_bush", List.of(1f, 0.1f),
		List.of(dye(DyeColor.RED), dye(DyeColor.GREEN)), List.of(2, 1)),

	RU_PURPLE_CONEFLOWER = ruFlower("purple_coneflower", List.of(1f),
		List.of(dye(DyeColor.PURPLE)), List.of(2)),

	RU_PURPLE_LUPINE = ruFlower("purple_lupine", List.of(1f),
		List.of(dye(DyeColor.PURPLE)), List.of(2)),

	RU_PURPLE_SNOWBELLE = ruFlower("purple_snowbelle", List.of(1f),
		List.of(dye(DyeColor.PURPLE)), List.of(2)),

	RU_RED_LUPINE = ruFlower("red_lupine", List.of(1f),
		List.of(dye(DyeColor.RED)), List.of(2)),

	RU_RED_SNOWBELLE = ruFlower("red_snowbelle", List.of(1f),
		List.of(dye(DyeColor.RED)), List.of(2)),

	RU_SALMON_POPPY_BUSH = ruFlower("salmon_poppy_bush", List.of(1f, 0.1f),
		List.of(dye(DyeColor.PINK), dye(DyeColor.GREEN)), List.of(2, 1)),

	RU_TASSEL = ruFlower("tassel", List.of(1f, 0.2f, 0.05f),
		List.of(dye(DyeColor.LIGHT_GRAY), dye(DyeColor.WHITE), dye(DyeColor.YELLOW)), List.of(2, 1, 1)),

	RU_TSUBAKI = ruFlower("tsubaki", List.of(1f, 0.1f),
		List.of(dye(DyeColor.RED), dye(DyeColor.GREEN)), List.of(2, 1)),

	RU_WARATAH = ruFlower("waratah", List.of(1f, 0.2f, 0.1f),
		List.of(dye(DyeColor.RED), dye(DyeColor.RED), dye(DyeColor.GREEN)), List.of(2, 1, 1)),

	RU_WHITE_SNOWBELLE = ruFlower("white_snowbelle", List.of(1f),
		List.of(dye(DyeColor.WHITE)), List.of(2)),

	RU_WHITE_TRILLIUM = ruFlower("white_trillium", List.of(1f, 0.2f, 0.05f),
		List.of(dye(DyeColor.LIGHT_GRAY), dye(DyeColor.WHITE), dye(DyeColor.YELLOW)), List.of(2, 1, 1)),

	RU_WILTING_TRILLIUM = ruFlower("wilting_trillium", List.of(1f, 0.1f),
		List.of(dye(DyeColor.BROWN), dye(DyeColor.LIGHT_GRAY)), List.of(2, 1)),

	RU_YELLOW_LUPINE = ruFlower("yellow_lupine", List.of(1f),
		List.of(dye(DyeColor.YELLOW)), List.of(2)),

	RU_YELLOW_SNOWBELLE = ruFlower("yellow_snowbelle", List.of(1f),
		List.of(dye(DyeColor.YELLOW)), List.of(2));

	GeneratedRecipe bopFlower(String input, List<Float> chances,
							  List<Item> dyes, List<Integer> amounts) {
		if (chances.size() == 2) {
			return create(Mods.BOP.recipeId(input), b -> b.duration(50)
				.require(Mods.BOP, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.whenModLoaded(Mods.BOP.getId()));
		} else if (chances.size() == 3) {
			return create(Mods.BOP.recipeId(input), b -> b.duration(50)
				.require(Mods.BOP, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.output(chances.get(2), dyes.get(2), amounts.get(2))
				.whenModLoaded(Mods.BOP.getId()));
		} else if (chances.size() == 1) {
			return create(Mods.BOP.recipeId(input), b -> b.duration(50)
				.require(Mods.BOP, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.whenModLoaded(Mods.BOP.getId()));
		} else {
			return null;
		}
	}

	GeneratedRecipe bwgFlower(String input, List<Float> chances,
							  List<Item> dyes, List<Integer> amounts) {
		if (chances.size() == 2) {
			return create(Mods.BWG.recipeId(input), b -> b.duration(50)
				.require(Mods.BWG, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.whenModLoaded(Mods.BWG.getId()));
		} else if (chances.size() == 3) {
			return create(Mods.BWG.recipeId(input), b -> b.duration(50)
				.require(Mods.BWG, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.output(chances.get(2), dyes.get(2), amounts.get(2))
				.whenModLoaded(Mods.BWG.getId()));
		} else if (chances.size() == 1) {
			return create(Mods.BWG.recipeId(input), b -> b.duration(50)
				.require(Mods.BWG, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.whenModLoaded(Mods.BWG.getId()));
		} else {
			return null;
		}
	}

	GeneratedRecipe envFlower(String input, List<Float> chances,
							  List<Item> dyes, List<Integer> amounts) {
		if (chances.size() == 2) {
			return create(Mods.ENV.recipeId(input), b -> b.duration(50)
				.require(Mods.ENV, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.whenModLoaded(Mods.ENV.getId()));
		} else if (chances.size() == 3) {
			return create(Mods.ENV.recipeId(input), b -> b.duration(50)
				.require(Mods.ENV, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.output(chances.get(2), dyes.get(2), amounts.get(2))
				.whenModLoaded(Mods.ENV.getId()));
		} else if (chances.size() == 1) {
			return create(Mods.ENV.recipeId(input), b -> b.duration(50)
				.require(Mods.ENV, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.whenModLoaded(Mods.ENV.getId()));
		} else {
			return null;
		}
	}

	GeneratedRecipe bopFlower(String input, Float chance, Item dye, int amount) {
		return create(Mods.BOP.recipeId(input), b -> b.duration(50)
			.require(Mods.BOP, input)
			.output(chance, dye, amount)
			.whenModLoaded(Mods.BOP.getId()));
	}

	GeneratedRecipe botaniaPetals(String... colors) {
		for (String color : colors) {
			create(Mods.BTN.recipeId(color + "_petal"), b -> b.duration(50)
				.require(TagKey.create(Registries.ITEM, Mods.BTN.asResource("petals/" + color)))
				.output(Mods.MC, color + "_dye")
				.whenModLoaded(Mods.BTN.getId()));
		}
		return null;
	}

	GeneratedRecipe ruFlower(String input, List<Float> chances,
							 List<Item> dyes, List<Integer> amounts) {
		if (chances.size() == 2) {
			return create(Mods.RU.recipeId(input), b -> b.duration(50)
				.require(Mods.RU, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.whenModLoaded(Mods.RU.getId()));
		} else if (chances.size() == 3) {
			return create(Mods.RU.recipeId(input), b -> b.duration(50)
				.require(Mods.RU, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.output(chances.get(1), dyes.get(1), amounts.get(1))
				.output(chances.get(2), dyes.get(2), amounts.get(2))
				.whenModLoaded(Mods.RU.getId()));
		} else if (chances.size() == 1) {
			return create(Mods.RU.recipeId(input), b -> b.duration(50)
				.require(Mods.RU, input)
				.output(chances.get(0), dyes.get(0), amounts.get(0))
				.whenModLoaded(Mods.RU.getId()));
		} else {
			return null;
		}
	}

	public CreateMillingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, Create.ID);
	}

	private static Item dye(DyeColor color) {
		return Items.DYE.pick(color);
	}
}
