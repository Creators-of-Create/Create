package com.simibubi.create.content.fluids.transfer;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.lang.Components;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FillingRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {

	public FillingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.FILLING, params);
	}

	@Override
	public boolean matches(RecipeWrapper inv, Level p_77569_2_) {
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

	@Override
	protected int getMaxFluidInputCount() {
		return 1;
	}

	public FluidIngredient getRequiredFluid() {
		if (fluidIngredients.isEmpty())
			throw new IllegalStateException("Filling Recipe: " + id.toString() + " has no fluid ingredient!");
		return fluidIngredients.get(0);
	}

	@Override
	public void addAssemblyIngredients(List<Ingredient> list) {}

	@Override
	public void addAssemblyFluidIngredients(List<FluidIngredient> list) {
		list.add(getRequiredFluid());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getDescriptionForAssembly() {
		List<FluidStack> matchingFluidStacks = fluidIngredients.get(0)
			.getMatchingFluidStacks();
		if (matchingFluidStacks.size() == 0)
			return Components.literal("Invalid");
		return CreateLang.translateDirect("recipe.assembly.spout_filling_fluid",
			matchingFluidStacks.get(0).getDisplayName().getString());
	}

	@Override
	public void addRequiredMachines(Set<ItemLike> list) {
		list.add(AllBlocks.SPOUT.get());
	}

	@Override
	public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
		return () -> SequencedAssemblySubCategory.AssemblySpouting::new;
	}

}
