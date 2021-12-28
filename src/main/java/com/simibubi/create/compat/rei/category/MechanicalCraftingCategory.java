package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.rei.category.animations.AnimatedCrafter;
import com.simibubi.create.compat.rei.display.MechanicalCraftingDisplay;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class MechanicalCraftingCategory extends CreateRecipeCategory<CraftingRecipe, MechanicalCraftingDisplay> {

	private final AnimatedCrafter crafter = new AnimatedCrafter();

	public MechanicalCraftingCategory() {
		super(itemIcon(AllBlocks.MECHANICAL_CRAFTER.get()), emptyBackground(177, 107));
	}

//	@Override
//	public void setIngredients(CraftingRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(recipe.getIngredients());
//		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
//	}

//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, CraftingRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();
//
//		itemStacks.init(0, false, 133, 80);
//		itemStacks.set(0, recipe.getResultItem());
//
//		int x = getXPadding(recipe);
//		int y = getYPadding(recipe);
//		float scale = getScale(recipe);
//		int size = recipeIngredients.size();
//		IIngredientRenderer<ItemStack> renderer = new CrafterIngredientRenderer(recipe);
//
//		for (int i = 0; i < size; i++) {
//			float f = 19 * scale;
//			int slotSize = (int) (16 * scale);
//			int xPosition = (int) (x + 1 + (i % getWidth(recipe)) * f);
//			int yPosition = (int) (y + 1 + (i / getWidth(recipe)) * f);
//			itemStacks.init(i + 1, true, renderer, xPosition, yPosition, slotSize, slotSize, 0, 0);
//			itemStacks.set(i + 1, Arrays.asList(recipeIngredients.get(i)
//				.getItems()));
//		}
//
//	}

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
	public List<Widget> setupDisplay(MechanicalCraftingDisplay display, Rectangle bounds) {
		List<Widget> widgets = new ArrayList<>();
		widgets.add(Widgets.createRecipeBase(bounds));
		Point origin = new Point(bounds.getX(), bounds.getY() + 4);
		CraftingRecipe recipe = display.getRecipe();
		float scale = getScale(recipe);
		Point offset = new Point(origin.getX() + getXPadding(recipe), origin.getY() + getYPadding(recipe));

		for (int row = 0; row < getHeight(recipe); row++)
			for (int col = 0; col < getWidth(recipe); col++)
				if (!recipe.getIngredients()
						.get(row * getWidth(recipe) + col)
						.isEmpty()) {
					Matrix4f matrix4f = Matrix4f.createScaleMatrix(scale, scale, scale);
					matrix4f.multiplyWithTranslation(offset.getX() + col * 19 * scale, offset.getY() + row * 19 * scale, 0);
					widgets.add(Widgets.withTranslate(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, 0, 0), matrix4f));
					widgets.add(Widgets.createSlot(new Point((offset.getX() + col * 19 * scale) + 1, (offset.getY() + row * 19 * scale) + 1)).disableBackground().markInput().entries(display.getInputEntries().get(row * getWidth(recipe) + col)));
				}

		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.getX() + 133, origin.getY() + 80));
		widgets.add(Widgets.createSlot(new Point(origin.getX() + 134, origin.getY() + 81)).disableBackground().markOutput().entries(display.getOutputEntries().get(0)));
		widgets.add(WidgetUtil.textured(AllGuiTextures.JEI_DOWN_ARROW, origin.getX() + 128, origin.getY() + 59));

		AnimatedCrafter crafter = new AnimatedCrafter();
		crafter.setPos(new Point(origin.getX() + 129, origin.getY() + 25));
		widgets.add(crafter);

		int amount = 0;
		for (Ingredient ingredient : recipe.getIngredients()) {
			if (Ingredient.EMPTY == ingredient)
				continue;
			amount++;
		}

		int finalAmount = amount;
		widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
			matrices.pushPose();
			RenderSystem.enableDepthTest();
			matrices.translate(0,0,300);
			Minecraft.getInstance().font.drawShadow(matrices, finalAmount + "", origin.getX() + 142, origin.getY() + 39, 0xFFFFFF);
			RenderSystem.disableDepthTest();
			matrices.popPose();
		}));

		return widgets;
	}


	// todo: idk if I should use this instead
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
					AllGuiTextures.JEI_SLOT.render(matrixStack, 0, 0);
					matrixStack.popPose();
				}

		matrixStack.popPose();

		AllGuiTextures.JEI_SLOT.render(matrixStack, 133, 80);
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 128, 59);
		crafter.draw(matrixStack, 129, 25);

		matrixStack.pushPose();
		matrixStack.translate(0, 0, 300);

		int amount = 0;
		for (Ingredient ingredient : recipe.getIngredients()) {
			if (Ingredient.EMPTY == ingredient)
				continue;
			amount++;
		}

		Minecraft.getInstance().font.drawShadow(matrixStack, amount + "", 142, 39, 0xFFFFFF);
		matrixStack.popPose();
	}

//	@Override
//	public Class<? extends CraftingRecipe> getRecipeClass() {
//		return CraftingRecipe.class;
//	}

//	private static final class CrafterIngredientRenderer implements IIngredientRenderer<ItemStack> {
//
//		private final CraftingRecipe recipe;
//
//		public CrafterIngredientRenderer(CraftingRecipe recipe) {
//			this.recipe = recipe;
//		}
//
//		@Override
//		public void render(PoseStack matrixStack, int xPosition, int yPosition, ItemStack ingredient) {
//			matrixStack.pushPose();
//			float scale = getScale(recipe);
//			matrixStack.scale(scale, scale, scale);
//
//			if (ingredient != null) {
//				PoseStack modelViewStack = RenderSystem.getModelViewStack();
//				modelViewStack.pushPose();
//				modelViewStack.mulPoseMatrix(matrixStack.last()
//					.pose());
//				RenderSystem.enableDepthTest();
//				Minecraft minecraft = Minecraft.getInstance();
//				Font font = getFontRenderer(minecraft, ingredient);
//				ItemRenderer itemRenderer = minecraft.getItemRenderer();
//				itemRenderer.renderAndDecorateFakeItem(ingredient, xPosition, yPosition);
//				itemRenderer.renderGuiItemDecorations(font, ingredient, xPosition, yPosition, null);
//				RenderSystem.disableBlend();
//				modelViewStack.popPose();
//			}
//
//			matrixStack.popPose();
//		}
//
//		@Override
//		public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
//			Minecraft minecraft = Minecraft.getInstance();
//			Player player = minecraft.player;
//			try {
//				return ingredient.getTooltipLines(player, tooltipFlag);
//			} catch (RuntimeException | LinkageError e) {
//				List<Component> list = new ArrayList<>();
//				TranslatableComponent crash = new TranslatableComponent("jei.tooltip.error.crash");
//				list.add(crash.withStyle(ChatFormatting.RED));
//				return list;
//			}
//		}
//	}

}
