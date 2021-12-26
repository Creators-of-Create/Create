package com.simibubi.create.content.contraptions.components.deployer;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.compat.rei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.contraptions.itemAssembly.IAssemblyRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.lib.transfer.item.RecipeWrapper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class DeployerApplicationRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {

	private boolean keepHeldItem;

	public DeployerApplicationRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.DEPLOYING, params);
		keepHeldItem = params.keepHeldItem;
	}

	@Override
	public boolean matches(RecipeWrapper inv, Level p_77569_2_) {
		return ingredients.get(0)
			.test(inv.getItem(0))
			&& ingredients.get(1)
				.test(inv.getItem(1));
	}

	@Override
	protected int getMaxInputCount() {
		return 2;
	}

	@Override
	protected int getMaxOutputCount() {
		return 2;
	}

	public boolean shouldKeepHeldItem() {
		return keepHeldItem;
	}

	public Ingredient getRequiredHeldItem() {
		if (ingredients.isEmpty())
			throw new IllegalStateException("Deploying Recipe: " + id.toString() + " has no tool!");
		return ingredients.get(1);
	}

	public Ingredient getProcessedItem() {
		if (ingredients.size() < 2)
			throw new IllegalStateException("Deploying Recipe: " + id.toString() + " has no ingredient!");
		return ingredients.get(0);
	}

	public static List<DeployerApplicationRecipe> convert(List<Recipe<?>> sandpaperRecipes) {
		return sandpaperRecipes.stream()
			.map(r -> new ProcessingRecipeBuilder<>(DeployerApplicationRecipe::new, new ResourceLocation(r.getId()
				.getNamespace(),
				r.getId()
					.getPath() + "_using_deployer")).require(r.getIngredients()
						.get(0))
						.require(AllItemTags.SANDPAPER.tag)
						.output(r.getResultItem())
						.build())
			.collect(Collectors.toList());
	}

	@Override
	public void addAssemblyIngredients(List<Ingredient> list) {
		list.add(ingredients.get(1));
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

	@Override
	@Environment(EnvType.CLIENT)
	public Component getDescriptionForAssembly() {
		ItemStack[] matchingStacks = ingredients.get(1)
			.getItems();
		if (matchingStacks.length == 0)
			return new TextComponent("Invalid");
		return Lang.translate("recipe.assembly.deploying_item",
			new TranslatableComponent(matchingStacks[0].getDescriptionId()).getString());
	}

	@Override
	public void addRequiredMachines(Set<ItemLike> list) {
		list.add(AllBlocks.DEPLOYER.get());
	}

	@Override
	public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
		return () -> SequencedAssemblySubCategory.AssemblyDeploying::new;
	}

}
