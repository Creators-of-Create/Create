package com.simibubi.create.content.contraptions.components.deployer;

import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class DeployerApplicationRecipe extends ProcessingRecipe<RecipeWrapper> {

	public DeployerApplicationRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.DEPLOYING, params);
	}

	@Override
	public boolean matches(RecipeWrapper inv, World p_77569_2_) {
		return ingredients.get(0)
			.test(inv.getStackInSlot(0))
			&& ingredients.get(1)
				.test(inv.getStackInSlot(1));
	}

	@Override
	protected int getMaxInputCount() {
		return 2;
	}

	@Override
	protected int getMaxOutputCount() {
		return 2;
	}

	public Ingredient getRequiredHeldItem() {
		if (ingredients.isEmpty())
			throw new IllegalStateException("Deploying Recipe: " + id.toString() + " has no tool!");
		return ingredients.get(0);
	}

	public Ingredient getProcessedItem() {
		if (ingredients.size() < 2)
			throw new IllegalStateException("Deploying Recipe: " + id.toString() + " has no ingredient!");
		return ingredients.get(1);
	}

	public static List<DeployerApplicationRecipe> convert(List<IRecipe<?>> sandpaperRecipes) {
		return sandpaperRecipes.stream()
			.map(r -> new ProcessingRecipeBuilder<>(DeployerApplicationRecipe::new, Create.asResource(r.getId()
				.getPath() + "_using_deployer"))
					.require(Ingredient.fromItems(AllItems.SAND_PAPER.get(), AllItems.RED_SAND_PAPER.get()))
					.require(r.getIngredients()
						.get(0))
					.output(r.getRecipeOutput())
					.build())
			.collect(Collectors.toList());
	}

}
