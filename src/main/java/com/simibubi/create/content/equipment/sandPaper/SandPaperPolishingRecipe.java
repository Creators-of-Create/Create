package com.simibubi.create.content.equipment.sandPaper;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@ParametersAreNonnullByDefault
public class SandPaperPolishingRecipe extends StandardProcessingRecipe<SingleRecipeInput> {

	public SandPaperPolishingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.SANDPAPER_POLISHING, params);
	}

	@Override
	public boolean matches(SingleRecipeInput inv, Level worldIn) {
		return ingredients.get(0)
			.test(inv.getItem(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

	public static boolean canPolish(Level world, ItemStack stack) {
		return !getMatchingRecipes(world, stack).isEmpty();
	}

	public static ItemStack applyPolish(Level world, Vec3 position, ItemStack stack, ItemStack sandPaperStack) {
		List<RecipeHolder<Recipe<SingleRecipeInput>>> matchingRecipes = getMatchingRecipes(world, stack);
		if (!matchingRecipes.isEmpty())
			return matchingRecipes.get(0).value()
				.assemble(new SingleRecipeInput(stack), world.registryAccess())
				.copy();
		return stack;
	}

	public static List<RecipeHolder<Recipe<SingleRecipeInput>>> getMatchingRecipes(Level world, ItemStack stack) {
		return world.getRecipeManager()
			.getRecipesFor(AllRecipeTypes.SANDPAPER_POLISHING.getType(), new SingleRecipeInput(stack), world);
	}
}
