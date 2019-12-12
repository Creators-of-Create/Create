package com.simibubi.create.compat.jei;

import java.util.Arrays;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.BlockState;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public abstract class ProcessingViaFanCategory<T extends IRecipe<?>> implements IRecipeCategory<T> {

	@Override
	public IDrawable getBackground() {
		return new ScreenResourceWrapper(ScreenResources.FAN_RECIPE);
	}

	@Override
	public void setIngredients(T recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 20, 67);
		itemStacks.set(0, Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));

		itemStacks.init(1, false, 139, 67);
		itemStacks.set(1, recipe.getRecipeOutput());
	}

	@Override
	public void draw(T recipe, double mouseX, double mouseY) {
		GlStateManager.pushMatrix();
		GlStateManager.color3f(1, 1, 1);
		GlStateManager.enableDepthTest();

		GlStateManager.translated(28, 42, 0);
		GlStateManager.rotated(10.5, -1f, 0, 0);
		GlStateManager.rotated(15.5, 0, 1, 0);
		GlStateManager.scaled(.6f, .6f, .6f);
		ScreenElementRenderer.renderBlock(this::renderFanCasing);

		GlStateManager.pushMatrix();
		float angle = AnimatedKinetics.getCurrentAngle() * 12;
		float t = 25;
		GlStateManager.translatef(t, -t, t);
		GlStateManager.rotated(angle, 0, 0, 1);
		GlStateManager.translatef(-t, t, -t);
		ScreenElementRenderer.renderBlock(this::renderFanInner);
		GlStateManager.popMatrix();

		GlStateManager.translated(-10, 0, 95);
		GlStateManager.rotated(7, 0, 1, 0);
		renderAttachedBlock();

		GlStateManager.popMatrix();

	}

	protected BlockState renderFanCasing() {

		return AllBlocks.ENCASED_FAN.get().getDefaultState().with(BlockStateProperties.FACING, Direction.WEST);
	}

	protected BlockState renderFanInner() {

		return AllBlocks.ENCASED_FAN_INNER.get().getDefaultState().with(BlockStateProperties.FACING, Direction.WEST);
	}

	public abstract void renderAttachedBlock();

}
