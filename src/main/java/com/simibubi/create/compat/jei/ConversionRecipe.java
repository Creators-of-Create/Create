package com.simibubi.create.compat.jei;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * Helper recipe type for displaying an item relationship in JEI
 * 
 * @author simibubi
 *
 */
@ParametersAreNonnullByDefault
public class ConversionRecipe extends ProcessingRecipe<RecipeWrapper> {

	public ConversionRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
		List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipeTypes.CONVERSION, id, group, ingredients, results, processingDuration);
	}

	static int counter = 0;

	public static ConversionRecipe create(ItemStack from, ItemStack to) {
		List<ProcessingIngredient> ingredients =
			Collections.singletonList(new ProcessingIngredient(Ingredient.fromStacks(from)));
		List<ProcessingOutput> outputs = Collections.singletonList(new ProcessingOutput(to, 1));
		return new ConversionRecipe(new ResourceLocation(Create.ID, "conversion_" + counter++), ingredients, outputs);
	}

	public ConversionRecipe(ResourceLocation id, List<ProcessingIngredient> ingredients,
		List<ProcessingOutput> results) {
		this(id, "conversions", ingredients, results, -1);
	}

	@Override
	public boolean matches(RecipeWrapper inv, World worldIn) {
		return false;
	}

}
