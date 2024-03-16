package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public class PressingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	SUGAR_CANE = create(() -> Items.SUGAR_CANE, b -> b.output(Items.PAPER)),

		PATH = create("path", b -> b.require(Ingredient.of(Items.GRASS_BLOCK, Items.DIRT))
			.output(Items.DIRT_PATH)),

		IRON = create("iron_ingot", b -> b.require(I.iron())
			.output(AllItems.IRON_SHEET.get())),
		GOLD = create("gold_ingot", b -> b.require(I.gold())
			.output(AllItems.GOLDEN_SHEET.get())),
		COPPER = create("copper_ingot", b -> b.require(I.copper())
			.output(AllItems.COPPER_SHEET.get())),
		BRASS = create("brass_ingot", b -> b.require(I.brass())
			.output(AllItems.BRASS_SHEET.get())),

		// Atmospheric
		ATM = moddedPaths(Mods.ATM, "crustose"),

		// Better End Forge

		BEF = moddedPaths(Mods.BEF, "amber_moss", "cave_moss", "chorus_nylium", "crystal_moss",
				"end_moss", "end_myclium", "jungle_moss", "pink_moss", "shadow_grass"),
		// Environmental
		ENV_MYCELIUM = create("compat/environmental/mycelium_path", b -> b.require(Blocks.MYCELIUM)
				.output(Mods.ENV, "mycelium_path")
				.whenModLoaded(Mods.ENV.getId())),

		ENV_PODZOL = create("compat/environmental/podzol_path", b -> b.require(Blocks.PODZOL)
				.output(Mods.ENV, "podzol_path")
				.whenModLoaded(Mods.ENV.getId())),

		// Oh The Biomes You'll Go

		BYG = moddedPaths(Mods.BYG, "lush_grass")

	;

	GeneratedRecipe moddedPaths(Mods mod, String... blocks) {
		for(String block : blocks) {
			moddedCompacting(mod, block, block + "_path");
		}
		return null;
	}

	GeneratedRecipe moddedCompacting(Mods mod, String input, String output) {
		return create("compat/" + mod.getId() + "/" + output, b -> b.require(mod, input)
				.output(mod, output)
				.whenModLoaded(mod.getId()));
	}

	public PressingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.PRESSING;
	}

}
