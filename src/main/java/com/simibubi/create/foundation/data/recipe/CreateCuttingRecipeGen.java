package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.CuttingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.I;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;

/**
 * Create's own Data Generation for Cutting recipes
 * @see CuttingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateCuttingRecipeGen extends CuttingRecipeGen {

	GeneratedRecipe
		ANDESITE_ALLOY = create(I::andesiteAlloy, b -> b.duration(200)
			.output(AllBlocks.SHAFT.get(), 6)),

		BAMBOO_PLANKS = create(() -> Blocks.BAMBOO_PLANKS, b -> b.duration(20)
			.output(Blocks.BAMBOO_MOSAIC, 1)),

	/*
	 * Mod compat
	 */

		// Ars Nouveau (all logs yield the same plank) (blue is covered by RuntimeDataGenerator to handle the planks into other recipes)
		ARS_N_1 = stripAndMakePlanks(Mods.ARS_N, null, "stripped_purple_archwood_log", "archwood_planks"),
		ARS_N_2 = stripAndMakePlanks(Mods.ARS_N, null, "stripped_green_archwood_log", "archwood_planks"),
		ARS_N_3 = stripAndMakePlanks(Mods.ARS_N, null, "stripped_red_archwood_log", "archwood_planks"),
		ARS_N_4 = stripAndMakePlanks(Mods.ARS_N, null, "stripped_purple_archwood_wood", "archwood_planks"),
		ARS_N_5 = stripAndMakePlanks(Mods.ARS_N, null, "stripped_green_archwood_wood", "archwood_planks"),
		ARS_N_6 = stripAndMakePlanks(Mods.ARS_N, null, "stripped_red_archwood_wood", "archwood_planks"),
		ARS_E_1 = stripAndMakePlanksDiffPlanksModId(Mods.ARS_E, null, "stripped_yellow_archwood_log", Mods.ARS_N, "archwood_planks"),
		ARS_E_2 = stripAndMakePlanksDiffPlanksModId(Mods.ARS_E, null, "stripped_yellow_archwood", Mods.ARS_N, "archwood_planks"),

		// Regions Unexplored
		RU_1 = stripAndMakePlanks(Mods.RU, "brimwood_log_magma", "stripped_brimwood_log", null),
		RU_2 = stripAndMakePlanks(Mods.RU, "ashen_log", "stripped_dead_log", null),
		RU_3 = stripAndMakePlanks(Mods.RU, "ashen_wood", "stripped_dead_wood", null),
		RU_4 = stripOnlyDiffModId(Mods.RU, "silver_birch_log", Mods.MC, "stripped_birch_log"),
		RU_5 = stripOnlyDiffModId(Mods.RU, "silver_birch_wood", Mods.MC, "stripped_birch_wood"),

		// Autumnity
		AUTUM_1 = stripAndMakePlanks(Mods.AUTUM, null, "sappy_maple_log", "maple_planks"),
		AUTUM_2 = stripAndMakePlanks(Mods.AUTUM, null, "sappy_maple_wood", "maple_planks"),

		// Endergetic Expansion
		ENDERGETIC_1 = stripAndMakePlanks(Mods.ENDER, "glowing_poise_stem", "stripped_poise_stem", null),
		ENDERGETIC_2 = stripAndMakePlanks(Mods.ENDER, "glowing_poise_wood", "stripped_poise_wood", null),

		// IE
		IE_WIRES = ieWires(CommonMetal.COPPER, CommonMetal.ELECTRUM, CommonMetal.ALUMINUM, CommonMetal.STEEL, CommonMetal.LEAD),

	// Jaden's Nether Expansion
	JNE_1 = stripAndMakePlanks(Mods.JNE, "cerebrage_claret_stem", "stripped_claret_stem", null),
		JNE_2 = stripAndMakePlanks(Mods.JNE, "cerebrage_claret_hyphae", "stripped_claret_hyphae", null),

		// Atmospheric
		ATM_1 = stripAndMakePlanks(Mods.ATM, "watchful_aspen_log", "aspen_log", null),
	    ATM_2 = stripAndMakePlanks(Mods.ATM, "watchful_aspen_wood", "aspen_wood", null),
		ATM_3 = stripAndMakePlanks(Mods.ATM, "crustose_log", "aspen_log", null),
		ATM_4 = stripAndMakePlanks(Mods.ATM, "crustose_wood", "aspen_wood", null)
		;

	public CreateCuttingRecipeGen(PackOutput output, CompletableFuture<Provider> registries) {
		super(output, registries, Create.ID);
	}

	GeneratedRecipe ieWires(CommonMetal... metals) {
		for (CommonMetal metal : metals)
			create(Mods.IE.recipeId("wire_" + metal), b -> b.duration(50)
				.require(metal.plates)
				.output(1, Mods.IE, "wire_" + metal, 2)
				.whenModLoaded(Mods.IE.getId()));
		return null;
	}
}
