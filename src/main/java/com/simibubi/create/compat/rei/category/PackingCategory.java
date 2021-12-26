package com.simibubi.create.compat.rei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.rei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.rei.category.animations.AnimatedPress;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class PackingCategory extends BasinCategory {

	private AnimatedPress press = new AnimatedPress(true);
	private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
	private PackingType type;

	enum PackingType {
		AUTO_SQUARE, COMPACTING;
	}

	public static PackingCategory standard() {
		return new PackingCategory(PackingType.COMPACTING, AllBlocks.BASIN.get(), 103);
	}

	public static PackingCategory autoSquare() {
		return new PackingCategory(PackingType.AUTO_SQUARE, Blocks.CRAFTING_TABLE, 85);
	}

	protected PackingCategory(PackingType type, ItemLike icon, int height) {
		super(type != PackingType.AUTO_SQUARE, doubleItemIcon(AllBlocks.MECHANICAL_PRESS, () -> icon),
			emptyBackground(177, height));
		this.type = type;
	}

//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, BasinRecipe recipe, IIngredients ingredients) {
//		if (type == PackingType.COMPACTING) {
//			super.setRecipe(recipeLayout, recipe, ingredients);
//			return;
//		}
//
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		int i = 0;
//
//		NonNullList<Ingredient> ingredients2 = recipe.getIngredients();
//		int size = ingredients2.size();
//		int rows = size == 4 ? 2 : 3;
//		while (i < size) {
//			Ingredient ingredient = ingredients2.get(i);
//			itemStacks.init(i, true, (rows == 2 ? 26 : 17) + (i % rows) * 19, 50 - (i / rows) * 19);
//			itemStacks.set(i, Arrays.asList(ingredient.getItems()));
//			i++;
//		}
//
//		itemStacks.init(i, false, 141, 50);
//		itemStacks.set(i, recipe.getResultItem());
//	}

	@Override
	public void draw(BasinRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		if (type == PackingType.COMPACTING) {
			super.draw(recipe, matrixStack, mouseX, mouseY);

		} else {
			NonNullList<Ingredient> ingredients2 = recipe.getIngredients();
			int size = ingredients2.size();
			int rows = size == 4 ? 2 : 3;
			for (int i = 0; i < size; i++)
				AllGuiTextures.JEI_SLOT.render(matrixStack, (rows == 2 ? 26 : 17) + (i % rows) * 19,
					50 - (i / rows) * 19);
			AllGuiTextures.JEI_SLOT.render(matrixStack, 141, 50);
			AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 136, 32);
			AllGuiTextures.JEI_SHADOW.render(matrixStack, 81, 68);
		}

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE)
			heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
				.draw(matrixStack, /*getBackground().getWidth() / 2 + */3, 55);
		press.draw(matrixStack, /*getBackground().getWidth() / 2 + */3, 34);
	}

}
