package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CuttingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	ANDESITE_ALLOY = create(I::andesite, b -> b.duration(200)
		.output(AllBlocks.SHAFT.get(), 6)),

		BAMBOO_PLANKS = create(() -> Blocks.BAMBOO_PLANKS, b -> b.duration(20)
			.output(Blocks.BAMBOO_MOSAIC, 1)),

		OAK_WOOD = stripAndMakePlanks(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD, Blocks.OAK_PLANKS),
		SPRUCE_WOOD = stripAndMakePlanks(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.SPRUCE_PLANKS),
		BIRCH_WOOD = stripAndMakePlanks(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.BIRCH_PLANKS),
		JUNGLE_WOOD = stripAndMakePlanks(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.JUNGLE_PLANKS),
		ACACIA_WOOD = stripAndMakePlanks(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.ACACIA_PLANKS),
		CHERRY_WOOD = stripAndMakePlanks(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD, Blocks.CHERRY_PLANKS),
		DARK_OAK_WOOD = stripAndMakePlanks(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.DARK_OAK_PLANKS),
		MANGROVE_WOOD = stripAndMakePlanks(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.MANGROVE_PLANKS),
		CRIMSON_WOOD = stripAndMakePlanks(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.CRIMSON_PLANKS),
		WARPED_WOOD = stripAndMakePlanks(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.WARPED_PLANKS),

		OAK_LOG = stripAndMakePlanks(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_PLANKS),
		SPRUCE_LOG = stripAndMakePlanks(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_PLANKS),
		BIRCH_LOG = stripAndMakePlanks(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_PLANKS),
		JUNGLE_LOG = stripAndMakePlanks(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_PLANKS),
		ACACIA_LOG = stripAndMakePlanks(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_PLANKS),
		CHERRY_LOG = stripAndMakePlanks(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_PLANKS),
		DARK_OAK_LOG = stripAndMakePlanks(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS),
		MANGROVE_LOG = stripAndMakePlanks(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_PLANKS),
		BAMBOO_BLOCK = stripAndMakePlanks(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.BAMBOO_PLANKS, 3),
		CRIMSON_LOG = stripAndMakePlanks(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_PLANKS),
		WARPED_LOG = stripAndMakePlanks(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_PLANKS),

		/*
		 * Mod compat
		 */

		// Ars Nouveau (all logs yield the same plank)
		ARS_N = cuttingCompat(Mods.ARS_N, "blue_archwood", "purple_archwood", "green_archwood", "red_archwood"),

		// Botania (stripped prefix is offset)
		BTN = cuttingCompat(Mods.BTN, "livingwood", "dreamwood"),
		BTN_2 = stripAndMakePlanks(Mods.BTN, "glimmering_livingwood_log", "glimmering_stripped_livingwood_log",
			"livingwood_planks"),
		BTN_3 = stripAndMakePlanks(Mods.BTN, "glimmering_livingwood", "glimmering_stripped_livingwood",
			"livingwood_planks"),
		BTN_4 = stripAndMakePlanks(Mods.BTN, "glimmering_dreamwood_log", "glimmering_stripped_dreamwood_log",
			"dreamwood_planks"),
		BTN_5 =
			stripAndMakePlanks(Mods.BTN, "glimmering_dreamwood", "glimmering_stripped_dreamwood", "dreamwood_planks"),

		// Forbidden Arcanus (no _wood suffix)
		FA = cuttingCompat(Mods.FA, "mysterywood"),

		// Hexcasting (stripped is a suffix here)
		HEX = cuttingCompat(Mods.HEX, "edified"),

		// Integrated Dynamics (stripped is a suffix here also)
		ID = cuttingCompat(Mods.ID, "menril"),

		// Oh The Biomes You'll Go
		BYG =
			cuttingCompat(Mods.BYG, "aspen", "baobab", "blue_enchanted", "cika", "cypress", "ebony", "ether",
				"fir", "green_enchanted", "holly", "jacaranda", "lament", "mahogany", "maple", "nightshade",
				"palm", "pine", "rainbow_eucalyptus", "redwood", "skyris", "willow", "witch_hazel", "zelkova"),
		BYG_2 = stripAndMakePlanks(Mods.BYG, "bulbis_stem", "stripped_bulbis_stem", "bulbis_planks"),
		BYG_3 = stripAndMakePlanks(Mods.BYG, "bulbis_wood", "stripped_bulbis_wood", "bulbis_planks"),
		BYG_4 = stripAndMakePlanks(Mods.BYG, null, "imparius_stem", "imparius_planks"),
		BYG_5 = stripAndMakePlanks(Mods.BYG, null, "imparius_hyphae", "imparius_planks"),
		BYG_6 = stripAndMakePlanks(Mods.BYG, null, "fungal_imparius_stem", "imparius_planks"),
		BYG_7 = stripAndMakePlanks(Mods.BYG, null, "fungal_imparius_hyphae", "imparius_planks"),
		BYG_8 = stripAndMakePlanks(Mods.BYG, "palo_verde_log", "stripped_palo_verde_log", null),
		BYG_9 = stripAndMakePlanks(Mods.BYG, "palo_verde_wood", "stripped_palo_verde_wood", null),

		// Silent Gear
		SG = cuttingCompat(Mods.SG, "netherwood"),

		// Twilight Forest
		TF = cuttingCompat(Mods.TF, "twilight_oak", "canopy", "mangrove", "dark", "time", "transformation", "mining",
			"sorting"),

		// Tinkers Construct
		TIC = cuttingCompat(Mods.TIC, "greenheart", "skyroot", "bloodshroom"),

		// Architects palette
		AP = cuttingCompat(Mods.AP, "twisted"),

		// Quark
		Q = cuttingCompat(Mods.Q, "azalea", "blossom"),

		// Ecologics
		ECO = cuttingCompat(Mods.ECO, "coconut", "walnut", "azalea"),
		ECO_2 = stripAndMakePlanks(Mods.ECO, "flowering_azalea_log", "stripped_azalea_log", null),
		ECO_3 = stripAndMakePlanks(Mods.ECO, "flowering_azalea_wood", "stripped_azalea_wood", null),

		// Biomes O' Plenty
		BOP = cuttingCompat(Mods.BOP, "fir", "redwood", "mahogany", "jacaranda", "palm", "willow", "dead",
			"magic", "umbran", "hellbark"),

		// Blue Skies (crystallized does not have stripped variants)
		BSK = cuttingCompat(Mods.BSK, "bluebright", "starlit", "frostbright", "lunar", "dusk", "maple"),
		BSK_2 = stripAndMakePlanks(Mods.BSK, null, "crystallized_log", "crystallized_planks"),
		BSK_3 = stripAndMakePlanks(Mods.BSK, null, "crystallized_wood", "crystallized_planks")

	;

	GeneratedRecipe stripAndMakePlanks(Block wood, Block stripped, Block planks) {
		return stripAndMakePlanks(wood, stripped, planks, 6);
	}

	GeneratedRecipe stripAndMakePlanks(Block wood, Block stripped, Block planks, int planksAmount) {
		create(() -> wood, b -> b.duration(50)
			.output(stripped));
		return create(() -> stripped, b -> b.duration(50)
			.output(planks, planksAmount));
	}

	GeneratedRecipe cuttingCompat(Mods mod, String... woodtypes) {
		for (String type : woodtypes) {
			String planks = type + "_planks";

			if (mod == Mods.ARS_N && type.contains("archwood"))
				planks = "archwood_planks";

			String strippedPre = mod.strippedIsSuffix ? "" : "stripped_";
			String strippedPost = mod.strippedIsSuffix ? "_stripped" : "";
			stripAndMakePlanks(mod, type + "_log", strippedPre + type + "_log" + strippedPost, planks);

			String wood = type + (mod.omitWoodSuffix ? "" : "_wood");
			stripAndMakePlanks(mod, wood, strippedPre + wood + strippedPost, planks);
		}
		return null;
	}

	GeneratedRecipe stripAndMakePlanks(Mods mod, String wood, String stripped, String planks) {
		if (wood != null)
			create("compat/" + mod.getId() + "/" + wood, b -> b.duration(50)
				.require(mod, wood)
				.output(1, mod, stripped, 1)
				.whenModLoaded(mod.getId()));
		if (planks != null)
			create("compat/" + mod.getId() + "/" + stripped, b -> b.duration(50)
				.require(mod, stripped)
				.output(1, mod, planks, 6)
				.whenModLoaded(mod.getId()));
		return null;
	}

	public CuttingRecipeGen(PackOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CUTTING;
	}

}
