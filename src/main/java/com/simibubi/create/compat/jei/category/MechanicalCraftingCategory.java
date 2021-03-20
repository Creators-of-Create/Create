package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class MechanicalCraftingCategory extends CreateRecipeCategory<ICraftingRecipe> {

	private final AnimatedCrafter crafter = new AnimatedCrafter();

	public MechanicalCraftingCategory() {
		super(itemIcon(AllBlocks.MECHANICAL_CRAFTER.get()), emptyBackground(177, 107));
	}

	@Override
	public void setIngredients(ICraftingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ICraftingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();

		itemStacks.init(0, false, 133, 80);
		itemStacks.set(0, recipe.getRecipeOutput()
			.getStack());

		int x = getXPadding(recipe);
		int y = getYPadding(recipe);
		float scale = getScale(recipe);
		int size = recipeIngredients.size();
		IIngredientRenderer<ItemStack> renderer = new CrafterIngredientRenderer(recipe);

		for (int i = 0; i < size; i++) {
			float f = 19 * scale;
			int slotSize = (int) (16 * scale);
			int xPosition = (int) (x + 1 + (i % getWidth(recipe)) * f);
			int yPosition = (int) (y + 1 + (i / getWidth(recipe)) * f);
			itemStacks.init(i + 1, true, renderer, xPosition, yPosition, slotSize, slotSize, 0, 0);
			itemStacks.set(i + 1, Arrays.asList(recipeIngredients.get(i)
				.getMatchingStacks()));
		}

	}

	static int maxSize = 100;

	public static float getScale(ICraftingRecipe recipe) {
		int w = getWidth(recipe);
		int h = getHeight(recipe);
		return Math.min(1, maxSize / (19f * Math.max(w, h)));
	}

	public static int getYPadding(ICraftingRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * getHeight(recipe) * 19 * .5);
	}

	public static int getXPadding(ICraftingRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * getWidth(recipe) * 19 * .5);
	}

	private static int getWidth(ICraftingRecipe recipe) {
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getWidth() : 1;
	}

	private static int getHeight(ICraftingRecipe recipe) {
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getHeight() : 1;
	}

	@Override
	public void draw(ICraftingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		matrixStack.push();
		float scale = getScale(recipe);
		matrixStack.translate(getXPadding(recipe), getYPadding(recipe), 0);

		for (int row = 0; row < getHeight(recipe); row++)
			for (int col = 0; col < getWidth(recipe); col++)
				if (!recipe.getIngredients()
					.get(row * getWidth(recipe) + col)
					.hasNoMatchingItems()) {
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

		Minecraft.getInstance().fontRenderer.drawWithShadow(matrixStack, amount + "", 142, 39, 0xFFFFFF);
		matrixStack.pop();
	}

	@Override
	public Class<? extends ICraftingRecipe> getRecipeClass() {
		return ICraftingRecipe.class;
	}

	private static final class CrafterIngredientRenderer implements IIngredientRenderer<ItemStack> {

		private final ICraftingRecipe recipe;

		public CrafterIngredientRenderer(ICraftingRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void render(MatrixStack matrixStack, int xPosition, int yPosition, ItemStack ingredient) {
			matrixStack.push();
			matrixStack.translate(xPosition, yPosition, 0);
			float scale = getScale(recipe);
			matrixStack.scale(scale, scale, scale);

			if (ingredient != null) {
				matrixStack.push();
				matrixStack.peek().getModel().multiply(matrixStack.peek().getModel());
				RenderSystem.enableDepthTest();
				RenderHelper.enable();
				Minecraft minecraft = Minecraft.getInstance();
				FontRenderer font = getFontRenderer(minecraft, ingredient);
				ItemRenderer itemRenderer = minecraft.getItemRenderer();
				itemRenderer.renderItemAndEffectIntoGUI(null, ingredient, 0, 0);
				itemRenderer.renderItemOverlayIntoGUI(font, ingredient, 0, 0, null);
				RenderSystem.disableBlend();
				RenderHelper.disableStandardItemLighting();
				matrixStack.pop();
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

}
