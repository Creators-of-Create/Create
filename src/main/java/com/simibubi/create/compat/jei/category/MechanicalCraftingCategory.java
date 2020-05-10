package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrafter;

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
import net.minecraft.item.Rarity;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class MechanicalCraftingCategory extends CreateRecipeCategory<ShapedRecipe> {

	private final class CrafterIngredientRenderer implements IIngredientRenderer<ItemStack> {

		private ShapedRecipe recipe;

		public CrafterIngredientRenderer(ShapedRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void render(int xPosition, int yPosition, ItemStack ingredient) {
			GlStateManager.pushMatrix();
			GlStateManager.translated(xPosition, yPosition, 0);
			float scale = getScale(recipe);
			GlStateManager.scaled(scale, scale, scale);

			if (ingredient != null) {
				GlStateManager.enableDepthTest();
				RenderHelper.enableGUIStandardItemLighting();
				Minecraft minecraft = Minecraft.getInstance();
				FontRenderer font = getFontRenderer(minecraft, ingredient);
				ItemRenderer itemRenderer = minecraft.getItemRenderer();
				itemRenderer.renderItemAndEffectIntoGUI(null, ingredient, 0, 0);
				itemRenderer.renderItemOverlayIntoGUI(font, ingredient, 0, 0, null);
				GlStateManager.disableBlend();
				RenderHelper.disableStandardItemLighting();
			}
			GlStateManager.popMatrix();
		}

		@Override
		public List<String> getTooltip(ItemStack ingredient, ITooltipFlag tooltipFlag) {
			Minecraft minecraft = Minecraft.getInstance();
			PlayerEntity player = minecraft.player;
			List<String> list;
			try {
				list = ingredient
						.getTooltip(player, tooltipFlag)
						.stream()
						.map(ITextComponent::getFormattedText)
						.collect(Collectors.toList());
			} catch (RuntimeException | LinkageError e) {
				return new ArrayList<>();
			}

			Rarity rarity;
			try {
				rarity = ingredient.getRarity();
			} catch (RuntimeException | LinkageError e) {
				rarity = Rarity.COMMON;
			}

			for (int k = 0; k < list.size(); ++k) {
				if (k == 0) {
					list.set(k, rarity.color + list.get(k));
				} else {
					list.set(k, TextFormatting.GRAY + list.get(k));
				}
			}

			return list;
		}
	}

	private AnimatedCrafter crafter = new AnimatedCrafter();

	public MechanicalCraftingCategory() {
		super("mechanical_crafting", itemIcon(AllBlocks.MECHANICAL_CRAFTER.get()), emptyBackground(177, 107));
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
	public void draw(ShapedRecipe recipe, double mouseX, double mouseY) {
		GlStateManager.pushMatrix();
		float scale = getScale(recipe);
		GlStateManager.translated(getXPadding(recipe), getYPadding(recipe), 0);

		for (int row = 0; row < recipe.getHeight(); row++)
			for (int col = 0; col < recipe.getWidth(); col++)
				if (!recipe.getIngredients().get(row * recipe.getWidth() + col).hasNoMatchingItems()) {
					GlStateManager.pushMatrix();
					GlStateManager.translated((int) col * 19 * scale, (int) row * 19 * scale, 0);
					GlStateManager.scaled(scale, scale, scale);
					ScreenResources.JEI_SLOT.draw(0, 0);
					GlStateManager.popMatrix();
				}

		GlStateManager.popMatrix();

		ScreenResources.JEI_SLOT.draw(133, 80);
		ScreenResources.JEI_DOWN_ARROW.draw(128, 59);
		ScreenResources.JEI_SHADOW.draw(116, 36);
		crafter.draw(219, 8);

		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 0, 200);
		
		RenderHelper.disableStandardItemLighting();
		int amount = 0;
		for (Ingredient ingredient : recipe.getIngredients()) {
			if (Ingredient.EMPTY == ingredient)
				continue;
			amount++;
		}
		Minecraft.getInstance().fontRenderer
				.drawStringWithShadow(amount + "", 142, 39, 0xFFFFFF);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();
	}

	@Override
	public Class<? extends ShapedRecipe> getRecipeClass() {
		return ShapedRecipe.class;
	}

}
