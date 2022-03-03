package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;
import com.simibubi.create.content.contraptions.processing.fan.custom.BlockStatePredicate;
import com.simibubi.create.content.contraptions.processing.fan.custom.CustomFanProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.fan.custom.CustomFanTypeConfig;
import com.simibubi.create.content.contraptions.processing.fan.custom.TypeCustom;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import mezz.jei.api.constants.VanillaTypes;
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

	public CustomFanCategory() {
		super(185, doubleItemIcon(AllItems.PROPELLER.get(), Items.BUCKET));
	}

	@Override
	public @NotNull Class<? extends CustomFanProcessingRecipe> getRecipeClass() {
		return CustomFanProcessingRecipe.class;
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
			ingredients.setInputs(VanillaTypes.FLUID, pred.fluids().stream().map(e -> new FluidStack(e, 0)).toList());
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

}
