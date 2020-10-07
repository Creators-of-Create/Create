package com.simibubi.create.foundation.config;

public class CRecipes extends ConfigBase {

	public ConfigBool allowShapelessInMixer = b(true, "allowShapelessInMixer", Comments.allowShapelessInMixer);
	public ConfigBool allowShapedSquareInPress = b(true, "allowShapedSquareInPress", Comments.allowShapedSquareInPress);
	public ConfigBool allowRegularCraftingInCrafter = b(true, "allowRegularCraftingInCrafter", Comments.allowRegularCraftingInCrafter);
	public ConfigBool allowStonecuttingOnSaw = b(true, "allowStonecuttingOnSaw", Comments.allowStonecuttingOnSaw);

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
	}

}
