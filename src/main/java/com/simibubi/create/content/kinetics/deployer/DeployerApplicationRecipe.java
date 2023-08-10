package com.simibubi.create.content.kinetics.deployer;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.lang.Components;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DeployerApplicationRecipe extends ItemApplicationRecipe implements IAssemblyRecipe {

	public DeployerApplicationRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.DEPLOYING, params);
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	public static DeployerApplicationRecipe convert(Recipe<?> sandpaperRecipe) {
		return new ProcessingRecipeBuilder<>(DeployerApplicationRecipe::new,
			new ResourceLocation(sandpaperRecipe.getId()
				.getNamespace(),
				sandpaperRecipe.getId()
					.getPath() + "_using_deployer")).require(sandpaperRecipe.getIngredients()
						.get(0))
						.require(AllItemTags.SANDPAPER.tag)
						.output(sandpaperRecipe.getResultItem())
						.build();
	}

	@Override
	public void addAssemblyIngredients(List<Ingredient> list) {
		list.add(ingredients.get(1));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getDescriptionForAssembly() {
		ItemStack[] matchingStacks = ingredients.get(1)
			.getItems();
		if (matchingStacks.length == 0)
			return Components.literal("Invalid");
		return CreateLang.translateDirect("recipe.assembly.deploying_item",
			Components.translatable(matchingStacks[0].getDescriptionId()).getString());
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
