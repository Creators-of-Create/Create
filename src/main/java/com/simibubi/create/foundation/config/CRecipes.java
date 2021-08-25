package com.simibubi.create.foundation.config;

public class CRecipes extends ConfigBase {

	public ConfigBool bulkPressing = b(false, "bulkPressing", Comments.bulkPressing);
	public ConfigBool bulkCutting = b(false, "bulkCutting", Comments.bulkCutting);
	public ConfigBool allowShapelessInMixer = b(true, "allowShapelessInMixer", Comments.allowShapelessInMixer);
	public ConfigBool allowShapedSquareInPress = b(true, "allowShapedSquareInPress", Comments.allowShapedSquareInPress);
	public ConfigBool allowRegularCraftingInCrafter =
		b(true, "allowRegularCraftingInCrafter", Comments.allowRegularCraftingInCrafter);
	public ConfigBool allowBiggerFireworksInCrafter =
		b(false, "allowBiggerFireworksInCrafter", Comments.allowBiggerFireworksInCrafter);
	public ConfigBool allowStonecuttingOnSaw = b(true, "allowStonecuttingOnSaw", Comments.allowStonecuttingOnSaw);
	public ConfigBool allowWoodcuttingOnSaw = b(true, "allowWoodcuttingOnSaw", Comments.allowWoodcuttingOnSaw);
	public ConfigBool allowCastingBySpout = b(true, "allowCastingBySpout", Comments.allowCastingBySpout);
	public ConfigInt lightSourceCountForRefinedRadiance =
		i(10, 1, "lightSourceCountForRefinedRadiance", Comments.refinedRadiance);
	public ConfigBool enableRefinedRadianceRecipe =
		b(true, "enableRefinedRadianceRecipe", Comments.refinedRadianceRecipe);
	public ConfigBool enableShadowSteelRecipe = b(true, "enableShadowSteelRecipe", Comments.shadowSteelRecipe);

	@Override
	public String getName() {
		return "recipes";
	}

	private static class Comments {
		static String bulkPressing = "Allow the Mechanical Press to process entire stacks at a time.";
		static String bulkCutting = "Allow the Mechanical Saw to process entire stacks at a time.";
		static String allowShapelessInMixer =
			"Allow allows any shapeless crafting recipes to be processed by a Mechanical Mixer + Basin.";
		static String allowShapedSquareInPress =
			"Allow any single-ingredient 2x2 or 3x3 crafting recipes to be processed by a Mechanical Press + Basin.";
		static String allowRegularCraftingInCrafter =
			"Allow any standard crafting recipes to be processed by Mechanical Crafters.";
		static String allowBiggerFireworksInCrafter =
			"Allow Firework Rockets with more than 9 ingredients to be crafted using Mechanical Crafters.";
		static String allowStonecuttingOnSaw =
			"Allow any stonecutting recipes to be processed by a Mechanical Saw.";
		static String allowWoodcuttingOnSaw =
			"Allow any Druidcraft woodcutter recipes to be processed by a Mechanical Saw.";
		static String allowCastingBySpout =
			"Allow Spouts to interact with Casting Tables and Basins from Tinkers' Construct.";
		static String refinedRadiance =
			"The amount of Light sources destroyed before Chromatic Compound turns into Refined Radiance.";
		static String refinedRadianceRecipe = "Allow the standard in-world Refined Radiance recipes.";
		static String shadowSteelRecipe = "Allow the standard in-world Shadow Steel recipe.";
	}

}
