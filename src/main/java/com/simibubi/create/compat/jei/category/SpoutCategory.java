package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class SpoutCategory extends CreateRecipeCategory<FillingRecipe> {

	AnimatedSpout spout;

	public SpoutCategory() {
		super(doubleItemIcon(AllBlocks.SPOUT.get(), Items.WATER_BUCKET), emptyBackground(177, 70));
		spout = new AnimatedSpout();
	}

	public static List<FillingRecipe> getRecipes(IIngredientManager ingredientManager) {
		List<FillingRecipe> recipes = new ArrayList<>();

		ingredientManager.getAllIngredients(VanillaTypes.ITEM)
			.stream()
			.forEach(stack -> {
				if (stack.getItem() instanceof PotionItem) {
					FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
					Ingredient bottle = Ingredient.fromItems(Items.GLASS_BOTTLE);
					recipes.add(new ProcessingRecipeBuilder<>(FillingRecipe::new, Create.asResource("potions"))
						.withItemIngredients(bottle)
						.withFluidIngredients(FluidIngredient.fromFluidStack(fluidFromPotionItem))
						.withSingleItemOutput(stack)
						.build());
					return;
				}

				LazyOptional<IFluidHandlerItem> capability =
					stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
				if (!capability.isPresent())
					return;

				ingredientManager.getAllIngredients(VanillaTypes.FLUID)
					.stream()
					.forEach(fluidStack -> {
						ItemStack copy = stack.copy();
						copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
							.ifPresent(fhi -> {
								FluidStack fluidCopy = fluidStack.copy();
								fluidCopy.setAmount(1000);
								fhi.fill(fluidCopy, FluidAction.EXECUTE);
								ItemStack container = fhi.getContainer();
								if (container.isItemEqual(copy))
									return;

								Ingredient bucket = Ingredient.fromStacks(stack);
								ResourceLocation itemName = stack.getItem()
									.getRegistryName();
								ResourceLocation fluidName = fluidCopy.getFluid()
									.getRegistryName();
								recipes.add(new ProcessingRecipeBuilder<>(FillingRecipe::new,
									Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
										+ "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()))
											.withItemIngredients(bucket)
											.withFluidIngredients(FluidIngredient.fromFluidStack(fluidCopy))
											.withSingleItemOutput(container)
											.build());
							});
					});
			});

		return recipes;
	}

	@Override
	public Class<? extends FillingRecipe> getRecipeClass() {
		return FillingRecipe.class;
	}

	@Override
	public void setIngredients(FillingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setInputLists(VanillaTypes.FLUID, recipe.getFluidIngredients()
			.stream()
			.map(FluidIngredient::getMatchingFluidStacks)
			.collect(Collectors.toList()));

		if (!recipe.getRollableResults()
			.isEmpty())
			ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		if (!recipe.getFluidResults()
			.isEmpty())
			ingredients.setOutputs(VanillaTypes.FLUID, recipe.getFluidResults());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FillingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		FluidIngredient fluidIngredient = recipe.getRequiredFluid();
		List<ItemStack> matchingIngredients = Arrays.asList(recipe.getIngredients()
			.get(0)
			.getMatchingStacks());

		fluidStacks.init(0, true, 27, 32);
		fluidStacks.set(0, withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()));
		itemStacks.init(0, true, 26, 50);
		itemStacks.set(0, matchingIngredients);
		itemStacks.init(1, false, 131, 50);
		itemStacks.set(1, recipe.getRecipeOutput());

		addFluidTooltip(fluidStacks, ImmutableList.of(fluidIngredient), Collections.emptyList());
	}

	@Override
	public void draw(FillingRecipe recipe, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.draw(26, 31);
		AllGuiTextures.JEI_SLOT.draw(26, 50);
		getRenderedSlot(recipe, 0).draw(131, 50);
		AllGuiTextures.JEI_SHADOW.draw(62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.draw(126, 29);
		spout.withFluids(recipe.getRequiredFluid()
			.getMatchingFluidStacks())
			.draw(getBackground().getWidth() / 2 - 13, 22);
	}

}
