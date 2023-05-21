package com.simibubi.create.compat.jei;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * Helper recipe type for displaying an item relationship in JEI
 */
@ParametersAreNonnullByDefault
public class ConversionRecipe extends ProcessingRecipe<RecipeWrapper> {

	static int counter = 0;

	public static ConversionRecipe create(ItemStack from, ItemStack to) {
		ResourceLocation recipeId = Create.asResource("conversion_" + counter++);
		return new ProcessingRecipeBuilder<>(ConversionRecipe::new, recipeId)
			.withItemIngredients(Ingredient.of(from))
			.withSingleItemOutput(to)
			.build();
	}

	public ConversionRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.CONVERSION, params);
	}

	@Override
	public boolean matches(RecipeWrapper inv, Level worldIn) {
		return false;
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

}
