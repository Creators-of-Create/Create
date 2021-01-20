package com.simibubi.create.foundation.config;

public class CRecipes extends ConfigBase {

	public ConfigBool allowShapelessInMixer = b(true, "allowShapelessInMixer", Comments.allowShapelessInMixer);
	public ConfigBool allowShapedSquareInPress = b(true, "allowShapedSquareInPress", Comments.allowShapedSquareInPress);
	public ConfigBool allowRegularCraftingInCrafter =
		b(true, "allowRegularCraftingInCrafter", Comments.allowRegularCraftingInCrafter);
	public ConfigBool allowStonecuttingOnSaw = b(true, "allowStonecuttingOnSaw", Comments.allowStonecuttingOnSaw);
	public ConfigBool allowWoodcuttingOnSaw = b(true, "allowWoodcuttingOnSaw", Comments.allowWoodcuttingOnSaw);
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
		static String allowShapelessInMixer =
			"When true, allows any shapeless crafting recipes to be processed by a Mechanical Mixer + Basin.";
		static String allowShapedSquareInPress =
			"When true, allows any single-ingredient 2x2 or 3x3 crafting recipes to be processed by a Mechanical Press + Basin.";
		static String allowRegularCraftingInCrafter =
			"When true, allows any standard crafting recipes to be processed by Mechanical Crafters.";
		static String allowStonecuttingOnSaw =
			"When true, allows any stonecutting recipes to be processed by a Mechanical Saw.";
		static String allowWoodcuttingOnSaw =
			"When true, allows any Druidcraft woodcutter recipes to be processed by a Mechanical Saw.";
		static String refinedRadiance =
			"The amount of Light sources destroyed before Chromatic Compound turns into Refined Radiance.";
		static String refinedRadianceRecipe = "Allow the standard in-world Refined Radiance recipes.";
		static String shadowSteelRecipe = "Allow the standard in-world Shadow Steel recipe.";
	}

}
