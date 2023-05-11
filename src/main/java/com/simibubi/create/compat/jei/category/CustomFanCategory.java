package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;
import com.simibubi.create.content.contraptions.processing.fan.custom.BlockStatePredicate;
import com.simibubi.create.content.contraptions.processing.fan.custom.CustomFanProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.fan.custom.CustomFanTypeConfig;
import com.simibubi.create.content.contraptions.processing.fan.custom.TypeCustom;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class CustomFanCategory extends ProcessingViaFanCategory.MultiOutput<CustomFanProcessingRecipe> {

	public CustomFanCategory(Info<CustomFanProcessingRecipe> info) {
		super(info);
	}


	@Override
	public void setIngredients(CustomFanProcessingRecipe recipe, IIngredients ingredients) {
		List<Ingredient> list = new ArrayList<>(recipe.getIngredients());
		TypeCustom type = (TypeCustom) AbstractFanProcessingType.valueOf(recipe.type);
		CustomFanTypeConfig.BlockPredicateConfig pred = type.getConfig().block();
		List<ItemLike> blocks = new ArrayList<>();
		for (Block b : pred.blocks()) {
			blocks.add(b);
		}
		for (BlockStatePredicate state : pred.blockStates()) {
			blocks.add(state.block);
		}
		for (Fluid fluid : pred.fluids()) {
			Item item = fluid.getBucket();
			if (item != Items.AIR) blocks.add(item);
		}
		list.add(Ingredient.of(blocks.toArray(new ItemLike[0])));
		ingredients.setInputIngredients(list);
		if (pred.fluids().size() > 0)
			ingredients.setInputs(VanillaTypes.FLUID, pred.fluids().stream().map(e -> new FluidStack(e, 1000)).toList());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
	}

	@Override
	public void renderAttachedBlock(PoseStack matrixStack, CustomFanProcessingRecipe recipe) {
		AbstractFanProcessingType type = AbstractFanProcessingType.valueOf(recipe.type);
		if (type instanceof TypeCustom custom) {
			GuiGameElement.of(Optional.ofNullable(custom.getConfig().block().getBlockForDisplay()).orElse(Blocks.AIR.defaultBlockState()))
					.scale(24)
					.atLocal(0, 0, 2)
					.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
					.render(matrixStack);
		}
	}

	@Override
	protected void renderWidgets(PoseStack matrixStack, CustomFanProcessingRecipe recipe, double mouseX, double mouseY) {
		super.renderWidgets(matrixStack, recipe, mouseX, mouseY);
		AllGuiTextures.JEI_SLOT.render(matrixStack, 110, 7);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CustomFanProcessingRecipe recipe, IIngredients ingredients) {
		super.setRecipe(recipeLayout, recipe, ingredients);
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(recipe.getRollableResults().size() + 1, true, 110, 7);
		itemStacks.set(recipe.getRollableResults().size() + 1, ingredients.getInputs(VanillaTypes.ITEM).get(1));
	}

}
