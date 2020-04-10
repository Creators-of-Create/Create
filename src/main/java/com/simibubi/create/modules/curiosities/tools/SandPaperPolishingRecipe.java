package com.simibubi.create.modules.curiosities.tools;

import java.util.List;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.modules.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;
import com.simibubi.create.modules.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.modules.curiosities.tools.SandPaperPolishingRecipe.SandPaperInv;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class SandPaperPolishingRecipe extends ProcessingRecipe<SandPaperInv> {

	public static DamageSource CURSED_POLISHING = new DamageSource("create.curse_polish").setExplosion();

	public SandPaperPolishingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
			List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipes.SANDPAPER_POLISHING, id, group, ingredients, results, processingDuration);
	}

	public static boolean canPolish(World world, ItemStack stack) {
		return !getMatchingRecipes(world, stack).isEmpty();
	}

	public static ItemStack applyPolish(World world, Vec3d position, ItemStack stack, ItemStack sandPaperStack) {
		List<IRecipe<SandPaperInv>> matchingRecipes = getMatchingRecipes(world, stack);
		if (!matchingRecipes.isEmpty())
			return matchingRecipes.get(0).getCraftingResult(new SandPaperInv(stack)).copy();
		return stack;
	}

	@Override
	public boolean matches(SandPaperInv inv, World worldIn) {
		return ingredients.get(0).test(inv.getStackInSlot(0));
	}

	public static List<IRecipe<SandPaperInv>> getMatchingRecipes(World world, ItemStack stack) {
		return world.getRecipeManager().getRecipes(AllRecipes.SANDPAPER_POLISHING.getType(), new SandPaperInv(stack),
				world);
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
