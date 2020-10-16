package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrafter;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MechanicalCraftingCategory extends CreateRecipeCategory<ShapedRecipe> {

	private static final class CrafterIngredientRenderer implements IIngredientRenderer<ItemStack> {

		private final ShapedRecipe recipe;

		public CrafterIngredientRenderer(ShapedRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void render(MatrixStack matrixStack, int xPosition, int yPosition, ItemStack ingredient) {
			matrixStack.push();
			matrixStack.translate(xPosition, yPosition, 0);
			float scale = getScale(recipe);
			matrixStack.scale(scale, scale, scale);

			if (ingredient != null) {
				RenderSystem.pushMatrix();
				RenderSystem.multMatrix(matrixStack.peek().getModel());
				RenderSystem.enableDepthTest();
				RenderHelper.enable();
				Minecraft minecraft = Minecraft.getInstance();
				FontRenderer font = getFontRenderer(minecraft, ingredient);
				ItemRenderer itemRenderer = minecraft.getItemRenderer();
				itemRenderer.renderItemAndEffectIntoGUI(null, ingredient, 0, 0);
				itemRenderer.renderItemOverlayIntoGUI(font, ingredient, 0, 0, null);
				RenderSystem.disableBlend();
				RenderHelper.disableStandardItemLighting();
				RenderSystem.popMatrix();
			}

			matrixStack.pop();
		}

		@Override
		public List<ITextComponent> getTooltip(ItemStack ingredient, ITooltipFlag tooltipFlag) {
			Minecraft minecraft = Minecraft.getInstance();
			PlayerEntity player = minecraft.player;
			try {
				return ingredient.getTooltip(player, tooltipFlag);
			} catch (RuntimeException | LinkageError e) {
				List<ITextComponent> list = new ArrayList<>();
				TranslationTextComponent crash = new TranslationTextComponent("jei.tooltip.error.crash");
				list.add(crash.formatted(TextFormatting.RED));
				return list;
			}
		}
	}

	private final AnimatedCrafter crafter = new AnimatedCrafter();

	public MechanicalCraftingCategory(boolean isForNormalCraftingOnly) {
		super(
				isForNormalCraftingOnly ? "mechanical_crafting" : "mechanical_crafting_exclusive",
				itemIcon(AllBlocks.MECHANICAL_CRAFTER.get()),
				emptyBackground(177, 107));
	}

	@Override
	public void setIngredients(ShapedRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ShapedRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();

		itemStacks.init(0, false, 133, 80);
		itemStacks.set(0, recipe.getRecipeOutput().getStack());

		int x = getXPadding(recipe);
		int y = getYPadding(recipe);
		float scale = getScale(recipe);
		int size = recipeIngredients.size();
		IIngredientRenderer<ItemStack> renderer = new CrafterIngredientRenderer(recipe);

		for (int i = 0; i < size; i++) {
			float f = 19 * scale;
			int slotSize = (int) (16 * scale);
			int xPosition = (int) (x + 1 + (i % recipe.getWidth()) * f);
			int yPosition = (int) (y + 1 + (i / recipe.getWidth()) * f);
			itemStacks.init(i + 1, true, renderer, xPosition, yPosition, slotSize, slotSize, 0, 0);
			itemStacks.set(i + 1, Arrays.asList(recipeIngredients.get(i).getMatchingStacks()));
		}

	}

	static int maxSize = 100;

	public static float getScale(ShapedRecipe recipe) {
		int w = recipe.getWidth();
		int h = recipe.getHeight();
		return Math.min(1, maxSize / (19f * Math.max(w, h)));
	}

	public static int getYPadding(ShapedRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * recipe.getHeight() * 19 * .5);
	}

	public static int getXPadding(ShapedRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * recipe.getWidth() * 19 * .5);
	}

	@Override
	public void draw(ShapedRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		matrixStack.push();
		float scale = getScale(recipe);
		matrixStack.translate(getXPadding(recipe), getYPadding(recipe), 0);

		for (int row = 0; row < recipe.getHeight(); row++)
			for (int col = 0; col < recipe.getWidth(); col++)
				if (!recipe.getIngredients().get(row * recipe.getWidth() + col).hasNoMatchingItems()) {
					matrixStack.push();
					matrixStack.translate(col * 19 * scale, row * 19 * scale, 0);
					matrixStack.scale(scale, scale, scale);
					AllGuiTextures.JEI_SLOT.draw(matrixStack, 0, 0);
					matrixStack.pop();
				}

		matrixStack.pop();

		AllGuiTextures.JEI_SLOT.draw(matrixStack, 133, 80);
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 128, 59);
		crafter.draw(matrixStack, 129, 25);

		matrixStack.push();
		matrixStack.translate(0, 0, 300);

		RenderHelper.disableStandardItemLighting();
		int amount = 0;
		for (Ingredient ingredient : recipe.getIngredients()) {
			if (Ingredient.EMPTY == ingredient)
				continue;
			amount++;
		}

		Minecraft.getInstance().fontRenderer
				.drawWithShadow(matrixStack, amount + "", 142, 39, 0xFFFFFF);
		matrixStack.pop();
	}

	@Override
	public Class<? extends ShapedRecipe> getRecipeClass() {
		return ShapedRecipe.class;
	}

}
