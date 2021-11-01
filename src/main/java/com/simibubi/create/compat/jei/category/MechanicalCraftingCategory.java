package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class MechanicalCraftingCategory extends CreateRecipeCategory<CraftingRecipe> {

	private final AnimatedCrafter crafter = new AnimatedCrafter();

	public MechanicalCraftingCategory() {
		super(itemIcon(AllBlocks.MECHANICAL_CRAFTER.get()), emptyBackground(177, 107));
	}

	@Override
	public void setIngredients(CraftingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CraftingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();

		itemStacks.init(0, false, 133, 80);
		itemStacks.set(0, recipe.getResultItem()
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
				.getItems()));
		}

	}

	static int maxSize = 100;

	public static float getScale(CraftingRecipe recipe) {
		int w = getWidth(recipe);
		int h = getHeight(recipe);
		return Math.min(1, maxSize / (19f * Math.max(w, h)));
	}

	public static int getYPadding(CraftingRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * getHeight(recipe) * 19 * .5);
	}

	public static int getXPadding(CraftingRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * getWidth(recipe) * 19 * .5);
	}

	private static int getWidth(CraftingRecipe recipe) {
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getWidth() : 1;
	}

	private static int getHeight(CraftingRecipe recipe) {
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getHeight() : 1;
	}

	@Override
	public void draw(CraftingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		matrixStack.pushPose();
		float scale = getScale(recipe);
		matrixStack.translate(getXPadding(recipe), getYPadding(recipe), 0);

		for (int row = 0; row < getHeight(recipe); row++)
			for (int col = 0; col < getWidth(recipe); col++)
				if (!recipe.getIngredients()
					.get(row * getWidth(recipe) + col)
					.isEmpty()) {
					matrixStack.pushPose();
					matrixStack.translate(col * 19 * scale, row * 19 * scale, 0);
					matrixStack.scale(scale, scale, scale);
					AllGuiTextures.JEI_SLOT.draw(matrixStack, 0, 0);
					matrixStack.popPose();
				}

		matrixStack.popPose();

		AllGuiTextures.JEI_SLOT.draw(matrixStack, 133, 80);
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 128, 59);
		crafter.draw(matrixStack, 129, 25);

		matrixStack.pushPose();
		matrixStack.translate(0, 0, 300);

		Lighting.turnOff();
		int amount = 0;
		for (Ingredient ingredient : recipe.getIngredients()) {
			if (Ingredient.EMPTY == ingredient)
				continue;
			amount++;
		}

		Minecraft.getInstance().font.drawShadow(matrixStack, amount + "", 142, 39, 0xFFFFFF);
		matrixStack.popPose();
	}

	@Override
	public Class<? extends CraftingRecipe> getRecipeClass() {
		return CraftingRecipe.class;
	}

	private static final class CrafterIngredientRenderer implements IIngredientRenderer<ItemStack> {

		private final CraftingRecipe recipe;

		public CrafterIngredientRenderer(CraftingRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void render(PoseStack matrixStack, int xPosition, int yPosition, ItemStack ingredient) {
			matrixStack.pushPose();
			matrixStack.translate(xPosition, yPosition, 0);
			float scale = getScale(recipe);
			matrixStack.scale(scale, scale, scale);

			if (ingredient != null) {
				RenderSystem.pushMatrix();
				RenderSystem.multMatrix(matrixStack.last().pose());
				RenderSystem.enableDepthTest();
				Lighting.turnBackOn();
				Minecraft minecraft = Minecraft.getInstance();
				Font font = getFontRenderer(minecraft, ingredient);
				ItemRenderer itemRenderer = minecraft.getItemRenderer();
				itemRenderer.renderAndDecorateItem(null, ingredient, 0, 0);
				itemRenderer.renderGuiItemDecorations(font, ingredient, 0, 0, null);
				RenderSystem.disableBlend();
				Lighting.turnOff();
				RenderSystem.popMatrix();
			}

			matrixStack.popPose();
		}

		@Override
		public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
			Minecraft minecraft = Minecraft.getInstance();
			Player player = minecraft.player;
			try {
				return ingredient.getTooltipLines(player, tooltipFlag);
			} catch (RuntimeException | LinkageError e) {
				List<Component> list = new ArrayList<>();
				TranslatableComponent crash = new TranslatableComponent("jei.tooltip.error.crash");
				list.add(crash.withStyle(ChatFormatting.RED));
				return list;
			}
		}
	}

}
