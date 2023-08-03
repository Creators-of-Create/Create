package com.simibubi.create.content.equipment.sandPaper;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe.SandPaperInv;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

@ParametersAreNonnullByDefault
public class SandPaperPolishingRecipe extends ProcessingRecipe<SandPaperInv> {

	public SandPaperPolishingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.SANDPAPER_POLISHING, params);
	}

	@Override
	public boolean matches(SandPaperInv inv, Level worldIn) {
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
		List<Recipe<SandPaperInv>> matchingRecipes = getMatchingRecipes(world, stack);
		if (!matchingRecipes.isEmpty())
			return matchingRecipes.get(0)
				.assemble(new SandPaperInv(stack))
				.copy();
		return stack;
	}

	public static List<Recipe<SandPaperInv>> getMatchingRecipes(Level world, ItemStack stack) {
		return world.getRecipeManager()
			.getRecipesFor(AllRecipeTypes.SANDPAPER_POLISHING.getType(), new SandPaperInv(stack), world);
	}

	public static class SandPaperInv extends RecipeWrapper {

		public SandPaperInv(ItemStack stack) {
			super(new ItemStackHandler(1));
			inv.setStackInSlot(0, stack);
		}

	}
	@Override
	protected boolean canSpecifyDuration() {
		return false;
	}
}
