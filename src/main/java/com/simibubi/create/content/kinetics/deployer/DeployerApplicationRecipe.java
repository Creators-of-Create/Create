package com.simibubi.create.content.kinetics.deployer;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

import net.neoforged.api.distmarker.Dist;

public class DeployerApplicationRecipe extends ItemApplicationRecipe implements IAssemblyRecipe {

	public DeployerApplicationRecipe(ItemApplicationRecipeParams params) {
		super(AllRecipeTypes.DEPLOYING, params);
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	public static RecipeHolder<DeployerApplicationRecipe> convert(RecipeHolder<?> sandpaperRecipe) {
		Identifier id = sandpaperRecipe.id()
			.identifier()
			.withSuffix("_using_deployer");
		DeployerApplicationRecipe recipe = new ItemApplicationRecipe.Builder<>(DeployerApplicationRecipe::new, id)
				.require(sandpaperRecipe.value().placementInfo().ingredients()
						.get(0))
						.require(AllItemTags.SANDPAPER.tag)
						.output(sandpaperRecipe.value().display()
							.stream()
							.findFirst()
							.map(display -> display.result()
								.resolveForFirstStack(ContextMap.EMPTY))
							.orElse(ItemStack.EMPTY))
						.build();

		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe);
	}

	@Override
	public void addAssemblyIngredients(List<Ingredient> list) {
		list.add(ingredients.get(1));
	}

	@Override
	public Component getDescriptionForAssembly() {
		ItemStack matchingStack = ingredients.get(1)
			.items()
			.findFirst()
			.map(ItemStack::new)
			.orElse(ItemStack.EMPTY);
		if (matchingStack.isEmpty()) {
            return Component.literal("Invalid");
        }
		return CreateLang.translateDirect("recipe.assembly.deploying_item",
			matchingStack.getHoverName().getString());
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
