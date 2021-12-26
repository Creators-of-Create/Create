package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.jei.category.animations.AnimatedSaw;
import com.simibubi.create.compat.jei.display.BlockCuttingDisplay;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class BlockCuttingCategory extends CreateRecipeCategory<CondensedBlockCuttingRecipe, BlockCuttingDisplay> {

	private AnimatedSaw saw = new AnimatedSaw();

	public BlockCuttingCategory(Item symbol) {
		super(doubleItemIcon(AllBlocks.MECHANICAL_SAW, () -> symbol), emptyBackground(177, 70)); // Items.STONE_BRICK_STAIRS
	}

//	@Override
//	public Class<? extends CondensedBlockCuttingRecipe> getRecipeClass() {
//		return CondensedBlockCuttingRecipe.class;
//	}
//
//	@Override
//	public void setIngredients(CondensedBlockCuttingRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, CondensedBlockCuttingRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		itemStacks.init(0, true, 4, 4);
//		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getItems()));
//
//		List<List<ItemStack>> results = recipe.getCondensedOutputs();
//		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
//			int xOffset = (outputIndex % 5) * 19;
//			int yOffset = (outputIndex / 5) * -19;
//
//			itemStacks.init(outputIndex + 1, false, 77 + xOffset, 47 + yOffset);
//			itemStacks.set(outputIndex + 1, results.get(outputIndex));
//		}
//	}

	@Override
	public void draw(CondensedBlockCuttingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(matrixStack, 4, 4);
		int size = Math.min(recipe.getOutputs().size(), 15);
		for (int i = 0; i < size; i++) {
			int xOffset = (i % 5) * 19;
			int yOffset = (i / 5) * -19;
			AllGuiTextures.JEI_SLOT.render(matrixStack, 77 + xOffset, 47 + yOffset);
		}
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 31, 6);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 33 - 17, 37 + 13);
		saw.draw(matrixStack, 33, 37);
	}

	public static class CondensedBlockCuttingRecipe extends StonecutterRecipe {

		List<ItemStack> outputs = new ArrayList<>();

		public CondensedBlockCuttingRecipe(Ingredient ingredient) {
			super(new ResourceLocation(""), "", ingredient, ItemStack.EMPTY);
		}

		public void addOutput(ItemStack stack) {
			outputs.add(stack);
		}

		public List<ItemStack> getOutputs() {
			return outputs;
		}

		public List<List<ItemStack>> getCondensedOutputs() {
			List<List<ItemStack>> result = new ArrayList<>();
			int index = 0;
			boolean firstPass = true;
			for (ItemStack itemStack : outputs) {
				if (firstPass)
					result.add(new ArrayList<>());
				result.get(index).add(itemStack);
				index++;
				if (index >= 15) {
					index = 0;
					firstPass = false;
				}
			}
			return result;
		}

		public static List<CondensedBlockCuttingRecipe> condenseRecipes(List<Recipe<?>> stoneCuttingRecipes) {
			List<CondensedBlockCuttingRecipe> condensed = new ArrayList<>();
			Recipes: for (Recipe<?> recipe : stoneCuttingRecipes) {
				Ingredient i1 = recipe.getIngredients().get(0);
				for (CondensedBlockCuttingRecipe condensedRecipe : condensed) {
					if (ItemHelper.matchIngredients(i1, condensedRecipe.getIngredients().get(0))) {
						condensedRecipe.addOutput(recipe.getResultItem());
						continue Recipes;
					}
				}
				CondensedBlockCuttingRecipe cr = new CondensedBlockCuttingRecipe(i1);
				cr.addOutput(recipe.getResultItem());
				condensed.add(cr);
			}
			return condensed;
		}

		@Override
		public boolean isSpecial() {
			return true;
		}

	}

}
