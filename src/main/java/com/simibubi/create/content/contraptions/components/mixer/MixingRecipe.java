package com.simibubi.create.content.contraptions.components.mixer;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity.BasinInputInventory;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;

public class MixingRecipe extends ProcessingRecipe<BasinInputInventory> {

	public MixingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
		List<ProcessingOutput> results, int processingDuration, List<FluidStack> fluidIngredients,
		List<FluidStack> fluidResults, int requiredHeat) {
		super(AllRecipeTypes.MIXING, id, group, ingredients, results, processingDuration, fluidIngredients,
			fluidResults, requiredHeat);
	}

	public static MixingRecipe of(IRecipe<?> recipe) {
		return new MixingRecipe(recipe.getId(), recipe.getGroup(), ProcessingIngredient.list(recipe.getIngredients()),
			Collections.singletonList(new ProcessingOutput(recipe.getRecipeOutput(), 1)), -1, null, null, 0);
	}

	@Override
	protected int getMaxInputCount() {
		return 9;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

	@Override
	protected boolean canHaveCatalysts() {
		return true;
	}

	@Override
	public boolean matches(BasinInputInventory inv, @Nonnull World worldIn) {
		if (inv.isEmpty())
			return false;

		NonNullList<Ingredient> ingredients = this.getIngredients();
		if (!ingredients.stream()
			.allMatch(Ingredient::isSimple))
			return false;

		List<ItemStack> remaining = new ArrayList<>();
		for (int slot = 0; slot < inv.getSizeInventory(); ++slot) {
			ItemStack itemstack = inv.getStackInSlot(slot);
			if (!itemstack.isEmpty()) {
				remaining.add(itemstack.copy());
			}
		}

		// sort by leniency
		List<Ingredient> sortedIngredients = new LinkedList<>(ingredients);
		sortedIngredients.sort(Comparator.comparingInt(i -> i.getMatchingStacks().length));
		Ingredients: for (Ingredient ingredient : sortedIngredients) {
			for (ItemStack stack : remaining) {
				if (stack.isEmpty())
					continue;
				if (ingredient.test(stack)) {
					stack.shrink(1);
					continue Ingredients;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	protected boolean canHaveFluidIngredient() {
		return true;
	}

	@Override
	protected boolean canHaveFluidOutput() {
		return true;
	}

	@Override
	protected boolean requiresHeating() {
		return this.requiredHeat > 0;
	}

	public int getHeatLevelRequired() {
		return requiredHeat;
	}
}
