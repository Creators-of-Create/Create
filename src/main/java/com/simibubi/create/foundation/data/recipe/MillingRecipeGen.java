package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.UnaryOperator;

public class MillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	GRANITE = create(() -> Blocks.GRANITE, b -> b.duration(200)
		.output(Blocks.RED_SAND)),

		WOOL = create("wool", b -> b.duration(100)
			.require(ItemTags.WOOL)
			.output(Items.STRING)),

		CLAY = create(() -> Blocks.CLAY, b -> b.duration(50)
			.output(Items.CLAY_BALL, 3)
			.output(.5f, Items.CLAY_BALL)),

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
			.output(.25f, Items.WHITE_DYE, 1)
			.output(.25f, Items.BONE_MEAL, 3)),

		CACTUS = create(() -> Blocks.CACTUS, b -> b.duration(50)
			.output(Items.GREEN_DYE, 2)
			.output(.1f, Items.GREEN_DYE, 1)
			.whenModMissing("quark")),

		SEA_PICKLE = create(() -> Blocks.SEA_PICKLE, b -> b.duration(50)
			.output(Items.LIME_DYE, 2)
			.output(.1f, Items.GREEN_DYE)),

		BONE_MEAL = create(() -> Items.BONE_MEAL, b -> b.duration(70)
			.output(Items.WHITE_DYE, 2)
			.output(.1f, Items.LIGHT_GRAY_DYE, 1)),

		COCOA_BEANS = create(() -> Items.COCOA_BEANS, b -> b.duration(70)
			.output(Items.BROWN_DYE, 2)
			.output(.1f, Items.BROWN_DYE, 1)),

		SADDLE = create(() -> Items.SADDLE, b -> b.duration(200)
			.output(Items.LEATHER, 2)
			.output(.5f, Items.LEATHER, 2)),

		SUGAR_CANE = create(() -> Items.SUGAR_CANE, b -> b.duration(50)
			.output(Items.SUGAR, 2)
			.output(.1f, Items.SUGAR)),

		BEETROOT = create(() -> Items.BEETROOT, b -> b.duration(70)
			.output(Items.RED_DYE, 2)
			.output(.1f, Items.BEETROOT_SEEDS)),

		INK_SAC = create(() -> Items.INK_SAC, b -> b.duration(100)
			.output(Items.BLACK_DYE, 2)
			.output(.1f, Items.GRAY_DYE)),

		CHARCOAL = create(() -> Items.CHARCOAL, b -> b.duration(100)
			.output(Items.BLACK_DYE, 1)
			.output(.1f, Items.GRAY_DYE, 2)),

		COAL = create(() -> Items.COAL, b -> b.duration(100)
			.output(Items.BLACK_DYE, 2)
			.output(.1f, Items.GRAY_DYE, 1)),

		LAPIS_LAZULI = create(() -> Items.LAPIS_LAZULI, b -> b.duration(100)
			.output(Items.BLUE_DYE, 2)
			.output(.1f, Items.BLUE_DYE)),

		AZURE_BLUET = create(() -> Blocks.AZURE_BLUET, b -> b.duration(50)
			.output(Items.LIGHT_GRAY_DYE, 2)
			.output(.1f, Items.WHITE_DYE, 2)),

		BLUE_ORCHID = create(() -> Blocks.BLUE_ORCHID, b -> b.duration(50)
			.output(Items.LIGHT_BLUE_DYE, 2)
			.output(.05f, Items.LIGHT_GRAY_DYE, 1)),

		FERN = create(() -> Blocks.FERN, b -> b.duration(50)
			.output(Items.GREEN_DYE)
			.output(.1f, Items.WHEAT_SEEDS)),

		LARGE_FERN = create(() -> Blocks.LARGE_FERN, b -> b.duration(50)
			.output(Items.GREEN_DYE, 2)
			.output(.5f, Items.GREEN_DYE)
			.output(.1f, Items.WHEAT_SEEDS)),

		LILAC = create(() -> Blocks.LILAC, b -> b.duration(100)
			.output(Items.MAGENTA_DYE, 3)
			.output(.25f, Items.MAGENTA_DYE)
			.output(.25f, Items.PURPLE_DYE)),

		PEONY = create(() -> Blocks.PEONY, b -> b.duration(100)
			.output(Items.PINK_DYE, 3)
			.output(.25f, Items.MAGENTA_DYE)
			.output(.25f, Items.PINK_DYE)),

		ALLIUM = create(() -> Blocks.ALLIUM, b -> b.duration(50)
			.output(Items.MAGENTA_DYE, 2)
			.output(.1f, Items.PURPLE_DYE, 2)
			.output(.1f, Items.PINK_DYE)),

		LILY_OF_THE_VALLEY = create(() -> Blocks.LILY_OF_THE_VALLEY, b -> b.duration(50)
			.output(Items.WHITE_DYE, 2)
			.output(.1f, Items.LIME_DYE)
			.output(.1f, Items.WHITE_DYE)),

		ROSE_BUSH = create(() -> Blocks.ROSE_BUSH, b -> b.duration(50)
			.output(Items.RED_DYE, 3)
			.output(.05f, Items.GREEN_DYE, 2)
			.output(.25f, Items.RED_DYE, 2)),

		SUNFLOWER = create(() -> Blocks.SUNFLOWER, b -> b.duration(100)
			.output(Items.YELLOW_DYE, 3)
			.output(.25f, Items.ORANGE_DYE)
			.output(.25f, Items.YELLOW_DYE)),

		OXEYE_DAISY = create(() -> Blocks.OXEYE_DAISY, b -> b.duration(50)
			.output(Items.LIGHT_GRAY_DYE, 2)
			.output(.2f, Items.WHITE_DYE)
			.output(.05f, Items.YELLOW_DYE)),

		POPPY = create(() -> Blocks.POPPY, b -> b.duration(50)
			.output(Items.RED_DYE, 2)
			.output(.05f, Items.GREEN_DYE)),

		DANDELION = create(() -> Blocks.DANDELION, b -> b.duration(50)
			.output(Items.YELLOW_DYE, 2)
			.output(.05f, Items.YELLOW_DYE)),

		CORNFLOWER = create(() -> Blocks.CORNFLOWER, b -> b.duration(50)
			.output(Items.BLUE_DYE, 2)),

		WITHER_ROSE = create(() -> Blocks.WITHER_ROSE, b -> b.duration(50)
			.output(Items.BLACK_DYE, 2)
			.output(.1f, Items.BLACK_DYE)),

		ORANGE_TULIP = create(() -> Blocks.ORANGE_TULIP, b -> b.duration(50)
			.output(Items.ORANGE_DYE, 2)
			.output(.1f, Items.LIME_DYE)),

		RED_TULIP = create(() -> Blocks.RED_TULIP, b -> b.duration(50)
			.output(Items.RED_DYE, 2)
			.output(.1f, Items.LIME_DYE)),

		WHITE_TULIP = create(() -> Blocks.WHITE_TULIP, b -> b.duration(50)
			.output(Items.WHITE_DYE, 2)
			.output(.1f, Items.LIME_DYE)),

		PINK_TULIP = create(() -> Blocks.PINK_TULIP, b -> b.duration(50)
			.output(Items.PINK_DYE, 2)
			.output(.1f, Items.LIME_DYE)),

		TALL_GRASS = create(() -> Blocks.TALL_GRASS, b -> b.duration(100)
			.output(.5f, Items.WHEAT_SEEDS)),
		GRASS = create(() -> Blocks.GRASS, b -> b.duration(50)
			.output(.25f, Items.WHEAT_SEEDS)),

		// AE2

		AE2_CERTUS = create(Mods.AE2.recipeId("certus_quartz"), b -> b.duration(200)
				.require(AllTags.forgeItemTag("gems/certus_quartz"))
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

		ATMO_GILIA = create(Mods.ATMO.recipeId("gilia"), b -> b.duration(50)
				.require(Mods.ATMO, "gilia")
				.output(Items.PURPLE_DYE, 2)
				.output(.1f, Items.MAGENTA_DYE, 2)
				.output(.1f, Items.PINK_DYE)
				.whenModLoaded(Mods.ATMO.getId())),

		ATMO_HOT_BRUSH = create(Mods.ATMO.recipeId("hot_monkey_brush"), b -> b.duration(50)
				.require(Mods.ATMO, "hot_monkey_brush")
				.output(Items.ORANGE_DYE, 2)
				.output(.05f, Items.RED_DYE)
				.output(.05f, Items.YELLOW_DYE)
				.whenModLoaded(Mods.ATMO.getId())),

		ATMO_SCALDING_BRUSH = create(Mods.ATMO.recipeId("scalding_monkey_brush"), b -> b.duration(50)
				.require(Mods.ATMO, "scalding_monkey_brush")
				.output(Items.RED_DYE, 2)
				.output(.1f, Items.RED_DYE, 2)
				.output(.1f, Items.ORANGE_DYE)
				.whenModLoaded(Mods.ATMO.getId())),

		ATMO_WARM_BRUSH = create(Mods.ATMO.recipeId("warm_monkey_brush"), b -> b.duration(50)
				.require(Mods.ATMO, "scalding_monkey_brush")
				.output(Items.YELLOW_DYE, 2)
				.output(.1f, Items.YELLOW_DYE, 2)
				.output(.1f, Items.ORANGE_DYE)
				.whenModLoaded(Mods.ATMO.getId())),

		ATMO_YUCCA_FLOWER = create(Mods.ATMO.recipeId("yucca_flower"), b -> b.duration(50)
				.require(Mods.ATMO, "yucca_flower")
				.output(Items.LIGHT_GRAY_DYE, 2)
				.output(.05f, Items.WHITE_DYE)
				.whenModLoaded(Mods.ATMO.getId())),

		// Autumnity

		AUTUM_CROCUS = create(Mods.AUTUM.recipeId("autumn_crocus"), b -> b.duration(50)
				.require(Mods.AUTUM, "autumn_crocus")
				.output(Items.MAGENTA_DYE, 2)
				.output(.1f, Items.PINK_DYE, 2)
				.output(.1f, Items.PURPLE_DYE)
				.whenModLoaded(Mods.AUTUM.getId())),

		// Biomes O' Plenty
		BOP_HYDRANGEA = bopFlower("blue_hydrangea", List.of(1f, .05f, .25f),
				List.of(Items.LIGHT_BLUE_DYE, Items.GREEN_DYE, Items.LIGHT_BLUE_DYE), List.of(3,2,2)),

		BOP_BLOSSOM = bopFlower("burning_blossom", List.of(1f,.1f),
				List.of(Items.ORANGE_DYE, Items.LIME_DYE), List.of(2,1)),

		BOP_GLOWFLOWER = bopFlower("glowflower", List.of(1f, .1f),
				List.of(Items.CYAN_DYE, Items.WHITE_DYE), List.of(2,1)),

		BOP_LAVENDER = bopFlower("lavender", List.of(1f, .05f),
				List.of(Items.PURPLE_DYE, Items.GREEN_DYE), List.of(2,1)),

		BOP_COSMOS = bopFlower("orange_cosmos", List.of(1f, .1f),
				List.of(Items.ORANGE_DYE, Items.LIME_DYE), List.of(2,1)),

		BOP_DAFFODIL = bopFlower("pink_daffodil", List.of(1f, .25f, .05f),
				List.of(Items.PINK_DYE, Items.MAGENTA_DYE, Items.CYAN_DYE), List.of(2,1,1)),

		BOP_HIBISCUS = bopFlower("pink_hibiscus", List.of(1f, .25f, .1f),
				List.of(Items.PINK_DYE, Items.YELLOW_DYE, Items.GREEN_DYE), List.of(2,1,1)),

		BOP_ROSE = bopFlower("rose", List.of(1f, .05f),
				List.of(Items.RED_DYE, Items.GREEN_DYE), List.of(2,1)),

		BOP_VIOLET = bopFlower("violet", 1f, Items.PURPLE_DYE,2),

		BOP_WILDFLOWER = bopFlower("wildflower", List.of(1f, .1f),
				List.of(Items.MAGENTA_DYE, Items.LIME_DYE), List.of(2,1)),

		BOP_LILY = bopFlower("wilted_lily", 1f, Items.GRAY_DYE,2),

		// Botania
		BTN_PETALS = botaniaPetals("black", "blue", "brown", "cyan", "gray", "green", "light_blue",
				"light_gray", "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow"),

		// Buzzier Bees

		BB_BUTTERCUP = create(Mods.BB.recipeId("buttercup"), b -> b.duration(50)
				.require(Mods.BB, "buttercup")
				.output(Items.YELLOW_DYE, 2)
				.output(.1f, Items.LIME_DYE)
				.whenModLoaded(Mods.BB.getId())),

		BB_PINK_CLOVER = create(Mods.BB.recipeId("pink_clover"), b -> b.duration(50)
				.require(Mods.BB, "buttercup")
				.output(Items.PINK_DYE, 2)
				.output(.1f, Items.LIME_DYE)
				.whenModLoaded(Mods.BB.getId())),

		BB_WHITE_CLOVER = create(Mods.BB.recipeId("white_clover"), b -> b.duration(50)
				.require(Mods.BB, "buttercup")
				.output(Items.WHITE_DYE, 2)
				.output(.1f, Items.LIME_DYE)
				.whenModLoaded(Mods.BB.getId())),

		// Oh The Biomes You'll Go

		BYG_ALLIUM_BUSH = bygFlower("allium_flower_bush", List.of(1f,.05f,.25f),
				List.of(Items.PURPLE_DYE, Items.GREEN_DYE, Items.MAGENTA_DYE), List.of(3,2,2)),

		BYG_BELLFLOWER = bygFlower("alpine_bellflower", List.of(1f,.1f,.1f),
				List.of(Items.PURPLE_DYE, Items.BLUE_DYE, Items.GREEN_DYE), List.of(2,2,1)),

		BYG_AMARANTH = bygFlower("amaranth", List.of(1f,.05f,.25f),
				List.of(Items.RED_DYE, Items.GREEN_DYE, Items.RED_DYE), List.of(3,2,2)),

		BYG_ANGELICA = bygFlower("angelica", List.of(1f,.1f),
				List.of(Items.WHITE_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_BEGONIA = bygFlower("begonia", List.of(1f,.1f),
				List.of(Items.RED_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_BISTORT = bygFlower("bistort", List.of(1f,.1f,.1f),
				List.of(Items.PINK_DYE, Items.RED_DYE, Items.GREEN_DYE), List.of(2,2,1)),

		BYG_BLACK_ROSE = bygFlower("black_rose", List.of(1f,.1f),
				List.of(Items.BLACK_DYE, Items.BLACK_DYE), List.of(2,1)),

		BYG_BLUE_SAGE = bygFlower("blue_sage", List.of(1f,.1f,.1f),
				List.of(Items.BLUE_DYE, Items.CYAN_DYE, Items.GREEN_DYE), List.of(2,2,1)),

		BYG_CALIFORNIA_POPPY = bygFlower("california_poppy", List.of(1f,.05f),
				List.of(Items.ORANGE_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_CROCUS = bygFlower("crocus", List.of(1f,.1f,.1f),
				List.of(Items.PURPLE_DYE, Items.BLUE_DYE, Items.GREEN_DYE), List.of(2,2,1)),

		BYG_CYAN_AMARANTH = bygFlower("cyan_amaranth", List.of(1f,.05f,.25f),
				List.of(Items.RED_DYE, Items.GREEN_DYE, Items.RED_DYE), List.of(3,2,2)),

		BYG_CYAN_ROSE = bygFlower("cyan_rose", List.of(1f,.1f),
				List.of(Items.CYAN_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_CYAN_TULIP = bygFlower("cyan_tulip", List.of(1f,.1f),
				List.of(Items.CYAN_DYE, Items.LIME_DYE), List.of(2,1)),

		BYG_DAFFODIL = bygFlower("daffodil", List.of(1f,.1f,.1f),
				List.of(Items.PINK_DYE, Items.GREEN_DYE, Items.MAGENTA_DYE), List.of(2,1,1)),

		BYG_DELPHINIUM = bygFlower("delphinium", List.of(1f,.1f),
				List.of(Items.BLUE_DYE, Items.BLUE_DYE), List.of(3,1)),

		BYG_FAIRY_SLIPPER = bygFlower("fairy_slipper", List.of(1f,.1f,.1f),
				List.of(Items.MAGENTA_DYE, Items.PINK_DYE, Items.YELLOW_DYE), List.of(2,2,1)),

		BYG_FIRECRACKER_BUSH = bygFlower("firecracker_flower_bush", List.of(1f,.05f,.25f),
				List.of(Items.PINK_DYE, Items.GREEN_DYE, Items.RED_DYE), List.of(3,2,2)),

		BYG_FOXGLOVE = bygFlower("foxglove", List.of(1f,.25f,.25f),
				List.of(Items.MAGENTA_DYE, Items.PINK_DYE, Items.YELLOW_DYE), List.of(2,1,1)),

		BYG_GREEN_TULIP = bygFlower("green_tulip", List.of(1f,.1f),
				List.of(Items.LIME_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_GUZMANIA = bygFlower("guzmania", List.of(1f,.25f,.25f),
				List.of(Items.MAGENTA_DYE, Items.PINK_DYE, Items.YELLOW_DYE), List.of(2,1,1)),

		BYG_HYDRANGEA = bygFlower("hydrangea_bush", List.of(1f,.1f,.1f),
				List.of(Items.PURPLE_DYE, Items.BLUE_DYE, Items.WHITE_DYE), List.of(2,2,1)),

		BYG_INCAN_LILY = bygFlower("incan_lily", List.of(1f,.1f,.1f),
				List.of(Items.ORANGE_DYE, Items.GREEN_DYE, Items.RED_DYE), List.of(2,1,1)),

		BYG_IRIS = bygFlower("iris", List.of(1f,.05f),
				List.of(Items.PURPLE_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_ORCHID = bygFlower("orchid", List.of(1f,.05f),
				List.of(Items.PINK_DYE, Items.WHITE_DYE), List.of(2,1)),

		BYG_KOVAN = bygFlower("kovan_flower", List.of(1f,.2f,.05f),
				List.of(Items.RED_DYE, Items.LIME_DYE, Items.GREEN_DYE), List.of(2,1,1)),

		BYG_LAZARUS_BELLFLOWER = bygFlower("lazarus_bellflower", List.of(1f,.1f),
				List.of(Items.MAGENTA_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_LOLIPOP = bygFlower("lolipop_flower", List.of(1f,.25f,.05f),
				List.of(Items.YELLOW_DYE, Items.YELLOW_DYE, Items.GREEN_DYE), List.of(2,1,1)),

		BYG_MAGENTA_AMARANTH = bygFlower("magenta_amaranth", List.of(1f,.05f,.25f),
				List.of(Items.MAGENTA_DYE, Items.GREEN_DYE, Items.MAGENTA_DYE), List.of(3,2,2)),

		BYG_MAGENTA_TULIP = bygFlower("magenta_tulip", List.of(1f,.1f),
				List.of(Items.MAGENTA_DYE, Items.LIME_DYE), List.of(2,1)),

		BYG_ORANGE_AMARANTH = bygFlower("orange_amaranth", List.of(1f,.05f,.25f),
				List.of(Items.RED_DYE, Items.GREEN_DYE, Items.RED_DYE), List.of(3,2,2)),

		BYG_DAISY = bygFlower("orange_daisy", List.of(1f,.2f,.05f),
				List.of(Items.ORANGE_DYE, Items.YELLOW_DYE, Items.LIME_DYE), List.of(2,1,1)),

		BYG_OSIRIA_ROSE = bygFlower("osiria_rose", List.of(1f,.1f),
				List.of(Items.BLACK_DYE, Items.BLACK_DYE), List.of(2,1)),

		BYG_PEACH_LEATHER = bygFlower("peach_leather_flower", List.of(1f,.25f),
				List.of(Items.PINK_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_PINK_ALLIUM = bygFlower("pink_allium", List.of(1f,.1f,.1f),
				List.of(Items.MAGENTA_DYE, Items.PINK_DYE, Items.PURPLE_DYE), List.of(2,2,1)),

		BYG_PINK_ALLIUM_BUSH = bygFlower("pink_allium_flower_bush", List.of(1f,.05f,.25f),
				List.of(Items.PURPLE_DYE, Items.GREEN_DYE, Items.MAGENTA_DYE), List.of(3,2,2)),

		BYG_PINK_ANEMONE = bygFlower("pink_anemone", List.of(1f,.1f),
				List.of(Items.PINK_DYE, Items.PURPLE_DYE), List.of(2,2)),

		BYG_PINK_DAFODIL = bygFlower("pink_daffodil", List.of(1f,.1f,.1f),
				List.of(Items.PINK_DYE, Items.GREEN_DYE, Items.WHITE_DYE), List.of(2,1,1)),

		BYG_PROTEA = bygFlower("protea_flower", List.of(1f,.1f,.05f),
				List.of(Items.MAGENTA_DYE, Items.LIME_DYE, Items.PURPLE_DYE), List.of(2,1,1)),

		BYG_PURPLE_AMARANTH = bygFlower("purple_amaranth", List.of(1f,.05f,.25f),
				List.of(Items.PURPLE_DYE, Items.GREEN_DYE, Items.PURPLE_DYE), List.of(3,2,2)),

		BYG_PURPLE_SAGE = bygFlower("purple_rose", List.of(1f,.1f),
				List.of(Items.PURPLE_DYE, Items.MAGENTA_DYE), List.of(2,1)),

		BYG_PURPLE_TULIP = bygFlower("purple_tulip", List.of(1f,.1f),
				List.of(Items.PURPLE_DYE, Items.LIME_DYE), List.of(2,1)),

		BYG_RICHEA = bygFlower("richea", List.of(1f,.1f,.05f),
				List.of(Items.MAGENTA_DYE, Items.PINK_DYE, Items.YELLOW_DYE), List.of(2,1,1)),

		BYG_ROSE = bygFlower("rose", List.of(1f,.1f),
				List.of(Items.RED_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_SILVER_VASE = bygFlower("silver_vase_flower", List.of(1f,.1f,.05f),
				List.of(Items.PINK_DYE, Items.GREEN_DYE, Items.WHITE_DYE), List.of(2,1,1)),

		BYG_SNOWDROPS = bygFlower("snowdrops", List.of(1f,.1f,.1f),
				List.of(Items.WHITE_DYE, Items.LIME_DYE, Items.WHITE_DYE), List.of(2,1,1)),

		BYG_TALL_ALLIUM = bygFlower("tall_allium", List.of(1f,.05f,.25f),
				List.of(Items.PURPLE_DYE, Items.PURPLE_DYE, Items.MAGENTA_DYE), List.of(3,2,2)),

		BYG_TALL_PINK_ALLIUM = bygFlower("tall_pink_allium", List.of(1f,.05f,.25f),
				List.of(Items.PINK_DYE, Items.PINK_DYE, Items.MAGENTA_DYE), List.of(3,2,2)),

		BYG_TORCH_GINGER = bygFlower("torch_ginger", List.of(1f,.1f),
				List.of(Items.RED_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_VIOLET_LEATHER = bygFlower("violet_leather_flower", List.of(1f,.25f),
				List.of(Items.BLUE_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_WHITE_ANEMONE = bygFlower("white_anemone", List.of(1f,.1f),
				List.of(Items.WHITE_DYE, Items.LIGHT_GRAY_DYE), List.of(2,2)),

		BYG_PUFFBALL = create(Mods.BYG.recipeId("white_puffball_cap"), b -> b.duration(150)
				.require(Mods.BYG, "white_puffball_cap")
				.output(.25f, Mods.BYG, "white_puffball_spores", 1)
				.whenModLoaded(Mods.BYG.getId())),

		BYG_WHITE_SAGE = bygFlower(Mods.BYG.recipeId("white_sage"), List.of(1f, .1f),
				List.of(Items.WHITE_DYE, Items.GRAY_DYE), List.of(2,1)),

		BYG_WINTER_CYCLAMEN = bygFlower(Mods.BYG.recipeId("winter_cyclamen"), List.of(1f, .1f),
				List.of(Items.CYAN_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_WINTER_ROSE = bygFlower("winter_rose", List.of(1f,.1f),
				List.of(Items.WHITE_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_WINTER_SCILLA = bygFlower("winter_scilla", List.of(1f,.1f),
				List.of(Items.LIGHT_BLUE_DYE, Items.GREEN_DYE), List.of(2,1)),

		BYG_YELLOW_DAFFODIL = bygFlower("yellow_daffodil", List.of(1f,.1f,.1f),
				List.of(Items.YELLOW_DYE, Items.GREEN_DYE, Items.PINK_DYE), List.of(2,1,1)),

		BYG_YELLOW_TULIP = bygFlower("yellow_tulip", List.of(1f,.1f),
				List.of(Items.YELLOW_DYE, Items.LIME_DYE), List.of(2,1)),

		// Environmental

		ENV_BIRD_OF_PARADISE = envFlower("bird_of_paradise", List.of(1f,.25f,.25f),
				List.of(Items.ORANGE_DYE, Items.BLUE_DYE, Items.RED_DYE), List.of(3,1,1)),

		ENV_BLUE_DELPHINIUM = envFlower("blue_delphinium", List.of(1f,.1f),
				List.of(Items.BLUE_DYE, Items.BLUE_DYE), List.of(3,1)),

		ENV_BLUEBELL = envFlower("bluebell", List.of(1f),
				List.of(Items.BLUE_DYE), List.of(2)),

		ENV_CARTWHEEL = envFlower("cartwheel", List.of(1f,.1f),
				List.of(Items.PINK_DYE, Items.ORANGE_DYE), List.of(2,1)),

		ENV_DIANTHUS = envFlower("dianthus", List.of(1f,.1f),
				List.of(Items.GREEN_DYE, Items.GREEN_DYE), List.of(2,1)),

		ENV_MAGENTA_HIBISCUS = envFlower("magenta_hibiscus", List.of(1f,.1f),
				List.of(Items.MAGENTA_DYE, Items.MAGENTA_DYE), List.of(2,1)),

		ENV_ORANGE_HIBISCUS = envFlower("orange_hibiscus", List.of(1f,.1f),
				List.of(Items.ORANGE_DYE, Items.ORANGE_DYE), List.of(2,1)),

		ENV_PINK_DELPHINIUM = envFlower("pink_delphinium", List.of(1f,.1f),
				List.of(Items.PINK_DYE, Items.PINK_DYE), List.of(3,1)),

		ENV_PINK_HIBISCUS = envFlower("pink_hibiscus", List.of(1f,.1f),
				List.of(Items.PINK_DYE, Items.PINK_DYE), List.of(2,1)),

		ENV_PURPLE_DELPHINIUM = envFlower("purple_delphinium", List.of(1f,.1f),
				List.of(Items.PURPLE_DYE, Items.PURPLE_DYE), List.of(3,1)),

		ENV_PURPLE_HIBISCUS = envFlower("purple_hibiscus", List.of(1f,.1f),
				List.of(Items.PURPLE_DYE, Items.PURPLE_DYE), List.of(2,1)),

		ENV_RED_HIBISCUS = envFlower("red_hibiscus", List.of(1f,.1f),
				List.of(Items.RED_DYE, Items.RED_DYE), List.of(2,1)),

		ENV_RED_LOTUS = envFlower("red_lotus_flower", List.of(1f,.1f),
				List.of(Items.RED_DYE, Items.RED_DYE), List.of(2,1)),

		ENV_VIOLET = envFlower("violet", List.of(1f,.1f),
				List.of(Items.PURPLE_DYE, Items.PURPLE_DYE), List.of(2,1)),

		ENV_WHITE_DELPHINIUM = envFlower("white_delphinium", List.of(1f,.1f),
				List.of(Items.WHITE_DYE, Items.WHITE_DYE), List.of(3,1)),

		ENV_WHITE_LOTUS_FLOWER = envFlower("white_lotus_flower", List.of(1f,.1f),
				List.of(Items.WHITE_DYE, Items.LIME_DYE), List.of(2,1)),

		ENV_YELLOW_HIBISCUS = envFlower("yellow_hibiscus", List.of(1f,.1f),
				List.of(Items.YELLOW_DYE, Items.YELLOW_DYE), List.of(2,1)),

		// Duidcraft
		DC_LAVENDER = create(Mods.DRUIDCRAFT.recipeId("lavender"), b -> b.duration(50)
				.require(Mods.DRUIDCRAFT, "lavender")
				.output(Items.PURPLE_DYE, 2)
				.output(.1f, Items.PURPLE_DYE)
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
				.output(.25f, Items.BLACK_DYE)
				.output(.25f, Items.BONE_MEAL, 3)
				.whenModLoaded(Mods.TIC.getId())),

		// Upgrade Aquatic

		UA_FLOWERING_RUSH = create(Mods.UA.recipeId("flowering_rush"), b -> b.duration(50)
				.require(Mods.UA, "flowering_rush")
				.output(Items.PINK_DYE, 3)
				.output(.25f, Items.PINK_DYE, 2)
				.whenModLoaded(Mods.UA.getId())),

		UA_PINK_SEAROCKET = create(Mods.UA.recipeId("pink_searocket"), b -> b.duration(50)
				.require(Mods.UA, "pink_searocket")
				.output(Items.PINK_DYE, 2)
				.output(.1f, Items.GREEN_DYE)
				.whenModLoaded(Mods.UA.getId())),

		UA_WHITE_SEAROCKET = create(Mods.UA.recipeId("white_searocket"), b -> b.duration(50)
				.require(Mods.UA, "white_searocket")
				.output(Items.WHITE_DYE, 2)
				.output(.1f, Items.GREEN_DYE)
				.whenModLoaded(Mods.UA.getId()))



		;

	protected GeneratedRecipe metalOre(String name, ItemEntry<? extends Item> crushed, int duration) {
		return create(name + "_ore", b -> b.duration(duration)
			.withCondition(new NotCondition(new TagEmptyCondition("forge", "ores/" + name)))
			.require(AllTags.forgeItemTag("ores/" + name))
			.output(crushed.get()));
	}

	protected <T extends ProcessingRecipe<?>> GeneratedRecipe bopFlower(String input, List<Float> chances,
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

	protected <T extends ProcessingRecipe<?>> GeneratedRecipe bygFlower(String input, List<Float> chances,
																		List<Item> dyes, List<Integer> amounts) {
		if (chances.size() == 2) {
			return create(Mods.BYG.recipeId(input), b -> b.duration(50)
					.require(Mods.BYG, input)
					.output(chances.get(0), dyes.get(0), amounts.get(0))
					.output(chances.get(1), dyes.get(1), amounts.get(1))
					.whenModLoaded(Mods.BYG.getId()));
		} else if (chances.size() == 3) {
			return create(Mods.BYG.recipeId(input), b -> b.duration(50)
					.require(Mods.BYG, input)
					.output(chances.get(0), dyes.get(0), amounts.get(0))
					.output(chances.get(1), dyes.get(1), amounts.get(1))
					.output(chances.get(2), dyes.get(2), amounts.get(2))
					.whenModLoaded(Mods.BYG.getId()));
		} else if (chances.size() == 1) {
			return create(Mods.BYG.recipeId(input), b -> b.duration(50)
					.require(Mods.BYG, input)
					.output(chances.get(0), dyes.get(0), amounts.get(0))
					.whenModLoaded(Mods.BYG.getId()));
		} else {
			return null;
		}
	}

	protected <T extends ProcessingRecipe<?>> GeneratedRecipe envFlower(String input, List<Float> chances,
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

	protected GeneratedRecipe bopFlower(String input, Float chance, Item dye, int amount) {
		return create(Mods.BOP.recipeId(input), b -> b.duration(50)
				.require(Mods.BOP, input)
				.output(chance, dye, amount)
				.whenModLoaded(Mods.BOP.getId()));
	}

	protected GeneratedRecipe botaniaPetals(String... colors) {
		for (String color : colors) {
			create(Mods.BTN.recipeId(color + "_petal"), b -> b.duration(50)
					.require(AllTags.optionalTag(ForgeRegistries.ITEMS,
							new ResourceLocation(Mods.BTN.getId(), "petals/" + color)))
					.output(Mods.MC, color + "_dye"));
		}
		return null;
	}

	public MillingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MILLING;
	}

}
