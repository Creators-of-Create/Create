package com.simibubi.create.compat.jei;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class ToolboxColoringRecipeMaker {

	// From JEI's ShulkerBoxColoringRecipeMaker
	public static Stream<RecipeHolder<CraftingRecipe>> createRecipes() {
		String group = "create.toolbox.color";
		ItemStack baseShulkerStack = AllBlocks.TOOLBOXES.get(DyeColor.BROWN)
			.asStack();
		Ingredient baseShulkerIngredient = Ingredient.of(baseShulkerStack.getItem());

		return Arrays.stream(DyeColor.values())
			.filter(dc -> dc != DyeColor.BROWN)
			.map(color -> {
				TagKey<Item> colorTag = color.getTag();
				Ingredient colorIngredient = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(colorTag));
				List<Ingredient> inputs = List.of(baseShulkerIngredient, colorIngredient);
				Block coloredShulkerBox = AllBlocks.TOOLBOXES.get(color)
					.get();
				ItemStack output = new ItemStack(coloredShulkerBox);
				ShapelessRecipe recipe = new ShapelessRecipe(new Recipe.CommonInfo(false),
					new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, group),
					ItemStackTemplate.fromStack(output), inputs);
				return new RecipeHolder<>(CreateRecipeCategory.recipeKey(Create.asResource(group + "/" + color)), recipe);
			});
	}

	private ToolboxColoringRecipeMaker() {}

}
