package com.simibubi.create.content.contraptions.components.deployer;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class DeployerRecipeSearchEvent extends Event {
	private final DeployerTileEntity tileEntity;
	private final RecipeWrapper inventory;
	@Nullable
	IRecipe<? extends IInventory> recipe = null;
	private int maxPriority = 0;

	public DeployerRecipeSearchEvent(DeployerTileEntity tileEntity, RecipeWrapper inventory) {
		this.tileEntity = tileEntity;
		this.inventory = inventory;
	}

	@Override
	public boolean isCancelable() {
		return true;
	}

	public DeployerTileEntity getTileEntity() {
		return tileEntity;
	}

	public RecipeWrapper getInventory() {
		return inventory;
	}

	// lazyness to not scan for recipes that aren't selected
	public boolean shouldAddRecipeWithPriority(int priority) {
		return !isCanceled() && priority > maxPriority;
	}

	@Nullable
	public IRecipe<? extends IInventory> getRecipe() {
		if (isCanceled())
			return null;
		return recipe;
	}

	public void addRecipe(Supplier<Optional<? extends IRecipe<? extends IInventory>>> recipeSupplier, int priority) {
		if (!shouldAddRecipeWithPriority(priority))
			return;
		recipeSupplier.get().ifPresent(newRecipe -> {
			this.recipe = newRecipe;
			maxPriority = priority;
		});
	}
}
