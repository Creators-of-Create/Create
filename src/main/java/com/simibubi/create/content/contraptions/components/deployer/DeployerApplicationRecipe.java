package com.simibubi.create.content.contraptions.components.deployer;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.contraptions.itemAssembly.IAssemblyRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class DeployerApplicationRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {

	private boolean keepHeldItem;

	public DeployerApplicationRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.DEPLOYING, params);
		keepHeldItem = false;
	}

	@Override
	public boolean matches(RecipeWrapper inv, World p_77569_2_) {
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

	public static List<DeployerApplicationRecipe> convert(List<IRecipe<?>> sandpaperRecipes) {
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
		keepHeldItem = JSONUtils.getAsBoolean(json, "keepHeldItem", false);
	}

	@Override
	public void writeAdditional(JsonObject json) {
		super.writeAdditional(json);
		if (keepHeldItem)
			json.addProperty("keepHeldItem", keepHeldItem);
	}

	@Override
	public void readAdditional(PacketBuffer buffer) {
		super.readAdditional(buffer);
		keepHeldItem = buffer.readBoolean();
	}

	@Override
	public void writeAdditional(PacketBuffer buffer) {
		super.writeAdditional(buffer);
		buffer.writeBoolean(keepHeldItem);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ITextComponent getDescriptionForAssembly() {
		ItemStack[] matchingStacks = ingredients.get(1)
			.getItems();
		if (matchingStacks.length == 0)
			return new StringTextComponent("Invalid");
		return Lang.translate("recipe.assembly.deploying_item",
			new TranslationTextComponent(matchingStacks[0].getDescriptionId()).getString());
	}

	@Override
	public void addRequiredMachines(Set<IItemProvider> list) {
		list.add(AllBlocks.DEPLOYER.get());
	}

	@Override
	public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
		return () -> SequencedAssemblySubCategory.AssemblyDeploying::new;
	}

}
