package com.simibubi.create.content.kinetics.deployer;

import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class ItemApplicationRecipe extends ProcessingRecipe<RecipeWrapper> {

	private boolean keepHeldItem;

	public ItemApplicationRecipe(AllRecipeTypes type, ProcessingRecipeParams params) {
		super(type, params);
		keepHeldItem = params.keepHeldItem;
	}

	@Override
	public boolean matches(RecipeWrapper inv, Level p_77569_2_) {
		return getProcessedItem().test(inv.getItem(0)) && getRequiredHeldItem().test(inv.getItem(1));
	}

	@Override
	protected int getMaxInputCount() {
		return 2;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	public boolean shouldKeepHeldItem() {
		return keepHeldItem;
	}

	public Ingredient getRequiredHeldItem() {
		if (ingredients.size() < 2)
			throw new IllegalStateException("Item Application Recipe: " + id.toString() + " has no tool!");
		return ingredients.get(1);
	}

	public Ingredient getProcessedItem() {
		if (ingredients.isEmpty())
			throw new IllegalStateException("Item Application Recipe: " + id.toString() + " has no ingredient!");
		return ingredients.get(0);
	}

	@Override
	public void readAdditional(JsonObject json) {
		super.readAdditional(json);
		keepHeldItem = GsonHelper.getAsBoolean(json, "keepHeldItem", false);
	}

	@Override
	public void writeAdditional(JsonObject json) {
		super.writeAdditional(json);
		if (keepHeldItem)
			json.addProperty("keepHeldItem", keepHeldItem);
	}

	@Override
	public void readAdditional(FriendlyByteBuf buffer) {
		super.readAdditional(buffer);
		keepHeldItem = buffer.readBoolean();
	}

	@Override
	public void writeAdditional(FriendlyByteBuf buffer) {
		super.writeAdditional(buffer);
		buffer.writeBoolean(keepHeldItem);
	}

}
