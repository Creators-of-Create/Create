package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe.SandPaperInv;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class SandPaperPolishingRecipe extends ProcessingRecipe<SandPaperInv> {

	public SandPaperPolishingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
		List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipeTypes.SANDPAPER_POLISHING, id, group, ingredients, results, processingDuration);
	}

	public static boolean canPolish(World world, ItemStack stack) {
		return !getMatchingRecipes(world, stack).isEmpty();
	}

	public static ItemStack applyPolish(World world, Vector3d position, ItemStack stack, ItemStack sandPaperStack) {
		List<IRecipe<SandPaperInv>> matchingRecipes = getMatchingRecipes(world, stack);
		if (!matchingRecipes.isEmpty())
			return matchingRecipes.get(0)
				.getCraftingResult(new SandPaperInv(stack))
				.copy();
		return stack;
	}

	public static List<IRecipe<SandPaperInv>> getMatchingRecipes(World world, ItemStack stack) {
		return world.getRecipeManager()
			.getRecipes(AllRecipeTypes.SANDPAPER_POLISHING.getType(), new SandPaperInv(stack), world);
	}

	@Override
	public boolean matches(SandPaperInv inv, World worldIn) {
		return ingredients.get(0)
			.test(inv.getStackInSlot(0));
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

	public static class SandPaperInv extends RecipeWrapper {

		public SandPaperInv(ItemStack stack) {
			super(new ItemStackHandler(1));
			inv.setStackInSlot(0, stack);
		}

	}

}
