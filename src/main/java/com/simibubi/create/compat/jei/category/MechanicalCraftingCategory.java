package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.crafting.ShapedRecipe;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrafter;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import org.joml.Matrix3x2fStack;
import org.joml.Matrix4fStack;

@ParametersAreNonnullByDefault
public class MechanicalCraftingCategory extends CreateRecipeCategory<CraftingRecipe> {

	private final AnimatedCrafter crafter = new AnimatedCrafter();

	public MechanicalCraftingCategory(Info<CraftingRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CraftingRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 81)
			.addItemStack(getResultItem(recipe));

		int x = getXPadding(recipe);
		int y = getYPadding(recipe);
		float scale = getScale(recipe);

		IIngredientRenderer<ItemStack> renderer = new CrafterIngredientRenderer(recipe);
		int i = 0;

		for (Optional<Ingredient> optionalIngredient : getGridIngredients(recipe)) {
			float f = 19 * scale;
			int xPosition = (int) (x + 1 + (i % getWidth(recipe)) * f);
			int yPosition = (int) (y + 1 + (i / getWidth(recipe)) * f);

			optionalIngredient.ifPresent(ingredient -> builder.addSlot(RecipeIngredientRole.INPUT, xPosition, yPosition)
				.setCustomRenderer(VanillaTypes.ITEM_STACK, renderer)
				.addIngredients(ingredient));

			i++;
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
		if (recipe instanceof MechanicalCraftingRecipe mechanicalCraftingRecipe)
			return mechanicalCraftingRecipe.getWidth();
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getWidth() : 1;
	}

	private static int getHeight(CraftingRecipe recipe) {
		if (recipe instanceof MechanicalCraftingRecipe mechanicalCraftingRecipe)
			return mechanicalCraftingRecipe.getHeight();
		return recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getHeight() : 1;
	}

	private static List<Optional<Ingredient>> getGridIngredients(CraftingRecipe recipe) {
		if (recipe instanceof MechanicalCraftingRecipe mechanicalCraftingRecipe)
			return mechanicalCraftingRecipe.getIngredients();
		if (recipe instanceof ShapedRecipe shapedRecipe)
			return shapedRecipe.getIngredients();
		return getIngredients(recipe).stream()
			.map(Optional::of)
			.toList();
	}

	@Override
	public void draw(CraftingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphicsExtractor graphics, double mouseX,
		double mouseY) {
		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		float scale = getScale(recipe);
		matrixStack.translate(getXPadding(recipe), getYPadding(recipe));
		List<Optional<Ingredient>> ingredients = getGridIngredients(recipe);

		for (int row = 0; row < getHeight(recipe); row++)
			for (int col = 0; col < getWidth(recipe); col++) {
				int pIndex = row * getWidth(recipe) + col;
				if (pIndex >= ingredients.size())
					break;
				if (ingredients.get(pIndex)
					.map(Ingredient::isEmpty)
					.orElse(true))
					continue;
				matrixStack.pushMatrix();
				matrixStack.translate(col * 19 * scale, row * 19 * scale);
				matrixStack.scale(scale, scale);
				AllGuiTextures.JEI_SLOT.render(graphics, 0, 0);
				matrixStack.popMatrix();
			}

		matrixStack.popMatrix();

		AllGuiTextures.JEI_SLOT.render(graphics, 133, 80);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 128, 59);
		crafter.draw(graphics, 129, 25);

		matrixStack.pushMatrix();

		int amount = 0;
		for (Optional<Ingredient> optionalIngredient : ingredients) {
			if (optionalIngredient.map(Ingredient::isEmpty)
				.orElse(true))
				continue;
			amount++;
		}

		graphics.text(Minecraft.getInstance().font, amount + "", 142, 39, 0xFFFFFF);
		matrixStack.popMatrix();
	}

	private static final class CrafterIngredientRenderer implements IIngredientRenderer<ItemStack> {

		private final CraftingRecipe recipe;
		private final float scale;

		public CrafterIngredientRenderer(CraftingRecipe recipe) {
			this.recipe = recipe;
			scale = getScale(recipe);
		}

		@Override
		public void render(GuiGraphicsExtractor graphics, @NotNull ItemStack ingredient) {
			Matrix3x2fStack matrixStack = graphics.pose();
			matrixStack.pushMatrix();
			float scale = getScale(recipe);
			matrixStack.scale(scale, scale);

			if (ingredient != null) {
				Matrix4fStack modelViewStack = LegacyRenderSystemBridge.getModelViewStack();
				modelViewStack.pushMatrix();
				LegacyRenderSystemBridge.applyModelViewMatrix();
				LegacyRenderSystemBridge.enableDepthTest();
				Minecraft minecraft = Minecraft.getInstance();
				Font font = getFontRenderer(minecraft, ingredient);
				graphics.item(ingredient, 0, 0);
				graphics.itemDecorations(font, ingredient, 0, 0, null);
				LegacyRenderSystemBridge.disableBlend();
				modelViewStack.popMatrix();
				LegacyRenderSystemBridge.applyModelViewMatrix();
			}

			matrixStack.popMatrix();
		}

		@Override
		public int getWidth() {
			return (int) (16 * scale);
		}

		@Override
		public int getHeight() {
			return (int) (16 * scale);
		}

		@Override
		public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
			Minecraft minecraft = Minecraft.getInstance();
			Player player = minecraft.player;
			try {
				return ingredient.getTooltipLines(Item.TooltipContext.of(minecraft.level), player, tooltipFlag);
			} catch (RuntimeException | LinkageError e) {
				List<Component> list = new ArrayList<>();
                MutableComponent crash = Component.translatable("jei.tooltip.error.crash");
				list.add(crash.withStyle(ChatFormatting.RED));
				return list;
			}
		}
	}

}
