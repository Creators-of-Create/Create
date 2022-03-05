package com.simibubi.create.content.contraptions.components.deployer;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

public class DeployerRecipeSearchEvent {
	private boolean canceled = false;
	private final DeployerTileEntity tileEntity;
	private final RecipeWrapper inventory;
	@Nullable
	Recipe<? extends Container> recipe = null;
	private int maxPriority = 0;

	public static final Event<DeployerRecipeSearchCallback> EVENT = EventFactory.createArrayBacked(DeployerRecipeSearchCallback.class, callbacks -> (event) -> {
		for (DeployerRecipeSearchCallback callback : callbacks) {
			callback.handle(event);
		}
	});

	@FunctionalInterface
	public interface DeployerRecipeSearchCallback {
		void handle(DeployerRecipeSearchEvent event);
	}

	public DeployerRecipeSearchEvent(DeployerTileEntity tileEntity, RecipeWrapper inventory) {
		this.tileEntity = tileEntity;
		this.inventory = inventory;
	}

//	@Override
//	public boolean isCancelable() {
//		return true;
//	}

	public void cancel() {
		canceled = true;
	}

	public DeployerTileEntity getTileEntity() {
		return tileEntity;
	}

	public RecipeWrapper getInventory() {
		return inventory;
	}

	// lazyness to not scan for recipes that aren't selected
	public boolean shouldAddRecipeWithPriority(int priority) {
		return !canceled && priority > maxPriority;
	}

	@Nullable
	public Recipe<? extends Container> getRecipe() {
		if (canceled)
			return null;
		return recipe;
	}

	public void addRecipe(Supplier<Optional<? extends Recipe<? extends Container>>> recipeSupplier, int priority) {
		if (!shouldAddRecipeWithPriority(priority))
			return;
		recipeSupplier.get().ifPresent(newRecipe -> {
			this.recipe = newRecipe;
			maxPriority = priority;
		});
	}
}
